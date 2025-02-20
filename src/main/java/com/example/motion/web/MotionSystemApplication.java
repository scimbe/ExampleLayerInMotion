package com.example.motion.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.motion.services")
public class MotionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotionSystemApplication.class, args);
    }
}
