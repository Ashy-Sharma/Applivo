package com.projects.applivo.emulator;

import com.projects.applivo.exception.EmulatorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
@Slf4j
public class AdbCommandExecutor {

    private final DockerEmulatorManager dockerEmulatorManager;

    private final EmulatorConfig emulatorConfig;
    private void sleepSeconds(Long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmulatorException(
                    "Interrupted while waiting for emulator boot."
            );
        }
    }

    public void waitForBoot(String containerId){

        long deadline = System.currentTimeMillis() + emulatorConfig.getBootTimeoutSeconds() * 1000L;

        log.info("Waiting for ADB daemon on container {}", containerId);
        while (System.currentTimeMillis() < deadline){
            try {
                String devices = dockerEmulatorManager.executeCommand(
                        containerId,
                        "adb",
                        "devices"
                );
                log.debug("ADB devices output: '{}'", devices);

                if (devices.contains("emulator")){
                    log.info("ADB connected to emulator on container {}",containerId);
                    sleepSeconds(2000L);
                    break;
                }
            }catch (Exception e) {
                log.debug("ADB not read yet: {}", e.getMessage());
            }

            sleepSeconds(5000L);
        }

        String output = null;
        log.info("Polling boot animation on container {}", containerId);
        while (System.currentTimeMillis() < deadline){

            output = dockerEmulatorManager.executeCommand(
                    containerId,
                    "adb",
                    "shell",
                    "getprop",
                    "init.svc.bootanim"
            ).trim();

            log.debug("Boot animation status: '{}'", output);

            if (output.equals("stopped")){
                log.info("Emulator booted successfully on conatinaer {}", containerId);
                return;
            }

            sleepSeconds(5000L);

        }
        log.debug("Boot poll result: '{}'", output);
        throw new EmulatorException("Emulator boot timed out.");

    }

    public void installApk(String containerId, String containerApkPath){
        String output = dockerEmulatorManager.executeCommand(
                containerId,
                "adb",
                "install",
                containerApkPath
        );

        log.debug("APK install output: '{}'", output);

        if (output.contains("Failure") || output.contains("FAILED") || output.contains("Exception")) {
            throw new EmulatorException("APK install failed: " + output);
        }

        log.info("APK installed successfully on container {}",containerId);
    }

    public byte[] takeScreenshot(String containerId){

        dockerEmulatorManager.executeCommand(
                containerId,
                "adb",
                "shell",
                "screencap",
                "-p",
                emulatorConfig.getScreenshotAndroidPath()
        );

        dockerEmulatorManager.executeCommand(
                containerId,
                "adb",
                "pull",
                emulatorConfig.getScreenshotAndroidPath(),
                emulatorConfig.getScreenshotContainerPath()
        );

        return dockerEmulatorManager.readFileFromContainer(
                containerId,
                emulatorConfig.getScreenshotContainerPath()
        );

    }

    public void tap(String containerId, int x, int y){
        dockerEmulatorManager.executeCommand(
                containerId,
                "adb",
                "shell",
                "input",
                "tap",
                String.valueOf(x),
                String.valueOf(y)
        );
    }

    public void swipe(String containerId, int x1, int y1, int x2, int y2, int duration){

        dockerEmulatorManager.executeCommand(
                containerId,
                "adb",
                "shell",
                "input",
                "swipe",
                String.valueOf(x1),
                String.valueOf(y1),
                String.valueOf(x2),
                String.valueOf(y2),
                String.valueOf(duration)
        );

    }

    public void inputText(String containerId, String text){

        String escaped = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace(" ", "%s")
                .replace("&", "\\&")
                .replace("|", "\\|")
                .replace(";", "\\;")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("<", "\\<")
                .replace(">", "\\>");

        dockerEmulatorManager.executeCommand(
                containerId,
                "adb",
                "shell",
                "input",
                "text",
                escaped
        );
    }

    public void pressKey(String containerId, int keyCode){

        dockerEmulatorManager.executeCommand(
                containerId,
                "adb",
                "shell",
                "input",
                "keyevent",
                String.valueOf(keyCode)
        );

    }

    public ScreenResolution getScreenResolution(String containerId){

        String output = dockerEmulatorManager.executeCommand(
                containerId,
                "adb",
                "shell",
                "wm",
                "size"
        );

        Pattern pattern = Pattern.compile("Physical size: (\\d+)x(\\d+)");
        Matcher matcher = pattern.matcher(output);

        if (!matcher.find()){
            throw new EmulatorException("Unable to determine screen resolution : " + output);
        }

        return new ScreenResolution(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2))
        );

    }

}
