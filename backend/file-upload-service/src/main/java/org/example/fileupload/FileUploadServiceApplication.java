package org.example.fileupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.example.fileupload") // Ensure beans are scanned
public class FileUploadServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileUploadServiceApplication.class, args);
    }
}
