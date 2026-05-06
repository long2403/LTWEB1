package com.example.lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.example.lab1", 
    "com.example.lab1.repository", 
    "com.example.lab1.entity",
    "com.example.lab1.service",
    "com.example.lab1.controller"
})
@EnableJpaRepositories(basePackages = "com.example.lab1.repository")
public class Lab1Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab1Application.class, args);
    }
}