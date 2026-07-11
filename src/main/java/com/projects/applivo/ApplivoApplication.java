package com.projects.applivo;

import com.projects.applivo.emulator.EmulatorConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableScheduling
@EnableMethodSecurity
@EnableAsync
public class ApplivoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplivoApplication.class, args);
    }

}
