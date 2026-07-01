package com.projects.applivo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApplivoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplivoApplication.class, args);
    }

}
