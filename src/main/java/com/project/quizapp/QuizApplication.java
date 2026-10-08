package com.project.quizapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuizApplication {

    public static final String APP_VERSION = "1.2.0";

    public static void main(String[] args) {
        SpringApplication.run(QuizApplication.class, args);
        System.out.println("\n====================================================");
        System.out.println("🚀 Quiz Application Backend Ready");
        System.out.println("📦 Version: " + APP_VERSION);
        System.out.println("☕ Java 25 + Spring Boot 4.1.1");
        System.out.println("🌐 Open Web App: http://localhost:8080");
        System.out.println("====================================================\n");
    }
}
