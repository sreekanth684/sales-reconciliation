package org.example.dataprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.example.dataprocessing") // Ensure beans are scanned
public class DataProcessingApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataProcessingApplication.class, args);
    }
}
