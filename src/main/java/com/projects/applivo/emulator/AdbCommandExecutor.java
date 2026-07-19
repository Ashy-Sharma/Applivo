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
        DockerEmulatorManager.CommandResult result = dockerEmulatorManager.executeCommandWithResult(
                containerId,
                "adb",
                "install",
                containerApkPath
        );

        String stdout = result.stdout();
        String stderr = result.stderr();

        log.debug("APK install stdout: '{}'", stdout);
        if (!stderr.isBlank()) {
            log.debug("APK install stderr: '{}'", stderr);
        }

        boolean succeeded = stdout.contains("Success");
        boolean explicitFailure = stdout.contains("Failure")
                || stdout.contains("FAILED")
                || stderr.contains("Failure")
                || stderr.contains("FAILED");

        if (!succeeded || explicitFailure) {
            String reason = extractFailureReason(stdout + " " + stderr);
            throw new EmulatorException("APK install failed: " + reason);
        }

        log.info("APK installed successfully on container {}", containerId);
    }

    private String extractFailureReason(String combinedOutput) {
        if (combinedOutput.contains("INSTALL_FAILED_NO_MATCHING_ABIS")) {
            return "This APK requires native libraries not supported by this emulator (architecture mismatch).";
        }
        if (combinedOutput.contains("INSTALL_FAILED_INSUFFICIENT_STORAGE")) {
            return "Emulator storage is full.";
        }
        if (combinedOutput.contains("INSTALL_FAILED_ALREADY_EXISTS")) {
            return "App is already installed on this emulator.";
        }
        return combinedOutput.trim();
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

    public void launchApp(String containerId, String packageName) {
        String output = dockerEmulatorManager.executeCommand(
                containerId,
                "adb", "shell", "monkey",
                "-p", packageName,
                "-c", "android.intent.category.LAUNCHER",
                "1"
        );
        log.debug("Launch output: '{}'", output);
        if (output.contains("Error") || output.contains("No activities found")) {
            throw new EmulatorException("Failed to launch app: " + output);
        }
    }

}
