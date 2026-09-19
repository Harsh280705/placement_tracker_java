package com.example.placementtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the application.
 * {@code @SpringBootApplication} = @Configuration + @EnableAutoConfiguration + @ComponentScan.
 * It tells Spring Boot to auto-configure Tomcat, MVC, Thymeleaf, JPA, etc.
 * based on the jars found in pom.xml.
 */
@SpringBootApplication
public class PlacementTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlacementTrackerApplication.class, args);
    }
}
