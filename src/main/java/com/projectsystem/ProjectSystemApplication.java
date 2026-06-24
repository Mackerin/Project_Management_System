package com.projectsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения системы управления проектами.
 * Запускает Spring Boot приложение и инициализирует все компоненты.
 *
 * @author Евдокимов Д.А.
 * @version 1.0
 */
@SpringBootApplication
public class ProjectSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectSystemApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  Система управления проектами запущена!");
        System.out.println("  URL: http://localhost:8080");
        System.out.println("===========================================");
    }
}