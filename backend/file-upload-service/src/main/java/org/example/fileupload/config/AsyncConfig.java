package org.example.fileupload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "fileUploadExecutor")
    public ThreadPoolTaskExecutor fileUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // Number of threads actively processing uploads
        executor.setMaxPoolSize(10);  // Max threads if more concurrent uploads occur
        executor.setQueueCapacity(50); // Number of files that can wait before execution
        executor.setThreadNamePrefix("FileUploadThread-");
        executor.initialize();
        return executor;
    }
}
