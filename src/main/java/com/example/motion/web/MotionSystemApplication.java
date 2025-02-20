package com.example.motion.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.example.motion.sys.data.IMotionDataRepository;
import com.example.motion.sys.data.InMemoryMotionDataRepository;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.motion.services")
public class MotionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotionSystemApplication.class, args);
    }

    @Bean
    public IMotionDataRepository motionDataRepository() {
        return new InMemoryMotionDataRepository();
    }

    @Bean
    public CustomErrorController customErrorController() {
        return new CustomErrorController();
    }
}
