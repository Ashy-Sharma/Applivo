package com.projects.applivo.emulator;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.projects.applivo.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class DockerEmulatorManager {

    public record CommandResult(byte[] stdoutBytes, String stderr) {
        public String stdout() {
            return new String(stdoutBytes, StandardCharsets.UTF_8);
        }
    }

    private final DockerClient dockerClient;

    private final EmulatorConfig emulatorConfig;

    public String createAndStartContainer(int adbPort, int vncPort, int consolePort){

        ExposedPort console = ExposedPort.tcp(emulatorConfig.getBaseConsolePort());
        ExposedPort adb = ExposedPort.tcp(emulatorConfig.getBaseAdbPort());
        ExposedPort vnc = ExposedPort.tcp(emulatorConfig.getBaseVncPort());

        Ports ports = new Ports();
        ports.bind(console, Ports.Binding.bindPort(consolePort));
        ports.bind(adb, Ports.Binding.bindPort(adbPort));
        ports.bind(vnc, Ports.Binding.bindPort(vncPort));

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(ports)
                .withDevices(new Device("rwm", "/dev/kvm", "/dev/kvm"))
                .withGroupAdd(List.of("1301"));

        CreateContainerResponse response = dockerClient
                .createContainerCmd(emulatorConfig.getDockerImage())
                .withHostConfig(hostConfig)
                .withExposedPorts(adb, vnc)
                .withEnv("WEB_VNC=true")
                .exec();

        dockerClient.startContainerCmd(response.getId()).exec();

        return response.getId();

    }

    public void stopAndRemoveContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId)
                    .withTimeout(10)
                    .exec();
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true)
                    .exec();
        } catch (NotFoundException e) {
            log.warn("Container {} not found during stop/remove — may already be gone.", containerId);
        }
    }

    public void copyFileToContainer(String containerId,
                                    Path localPath,
                                    String containerDestPath) {

        try (ByteArrayOutputStream tarBytes = new ByteArrayOutputStream();
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tarBytes);
             InputStream fileInput = Files.newInputStream(localPath)) {

            TarArchiveEntry entry = new TarArchiveEntry(localPath.getFileName().toString());

            entry.setSize(Files.size(localPath));

            tarOut.putArchiveEntry(entry);

            fileInput.transferTo(tarOut);

            tarOut.closeArchiveEntry();
            tarOut.finish();

            try (ByteArrayInputStream tarInput =
                         new ByteArrayInputStream(tarBytes.toByteArray())){

                dockerClient.copyArchiveToContainerCmd(containerId)
                        .withTarInputStream(tarInput)
                        .withRemotePath(containerDestPath)
                        .exec();
            }

        } catch (IOException e) {
            throw new StorageException(
                    "Failed to copy file " + localPath + " to container " + containerId , e);
        }
    }

    public byte[] readFileFromContainer(String containerId, String containerPath){

        try (InputStream input = dockerClient
                .copyArchiveFromContainerCmd(containerId, containerPath)
                .exec();

             TarArchiveInputStream tarInput =
                     new TarArchiveInputStream(input)) {

            TarArchiveEntry entry = tarInput.getNextTarEntry();

            if (entry == null) {
                throw new RuntimeException(
                        "No file found at " + containerPath + " inside the container."
                );
            }

            return tarInput.readAllBytes();

        }catch (IOException e) {
            throw new RuntimeException(
                    "Failed to read file " + containerPath + " from container", e);
        }
    }

    public boolean isContainerRunning(String containerId){
        try {
            return Boolean.TRUE.equals(dockerClient.inspectContainerCmd(containerId)
                    .exec()
                    .getState()
                    .getRunning());

        }catch (NotFoundException e){
            return false;
        }
    }

    public int getRunningContainerCount(){
        return dockerClient.listContainersCmd()
                .withStatusFilter(List.of("running"))
                .withAncestorFilter(List.of(emulatorConfig.getDockerImage()))
                .exec()
                .size();
    }

    public byte[] executeCommandRaw(String containerId, String... command) {
        CommandResult result = runExec(createExec(containerId, command));
        if (!result.stderr().isBlank()) {
            log.warn("Command stderr: {}", result.stderr());
        }
        return result.stdoutBytes();
    }


    public CommandResult executeCommandWithResult(String containerId, String... command) {
        return runExec(createExec(containerId, command));
    }


    public String executeCommand(String containerId, String... command) {
        CommandResult result = runExec(createExec(containerId, command));

        if (!result.stderr().isBlank()) {
            log.warn("Command stderr: {}", result.stderr());
        }

        return result.stdout();
    }

    private ExecCreateCmdResponse createExec(String containerId, String... command) {
        return dockerClient.execCreateCmd(containerId)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(command)
                .exec();
    }

    private CommandResult runExec(ExecCreateCmdResponse exec) {
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            dockerClient.execStartCmd(exec.getId())
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            try {
                                switch (frame.getStreamType()) {
                                    case STDOUT -> stdout.write(frame.getPayload());
                                    case STDERR -> stderr.write(frame.getPayload());
                                    default -> {
                                    }
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .awaitCompletion();

            return new CommandResult(
                    stdout.toByteArray(),
                    stderr.toString(StandardCharsets.UTF_8)
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while executing command.", e);
        }
    }

}