package org.example.dataprocessing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ColumnMappingAsyncConfig {

    @Bean(name = "columnMappingExecutor")
    public ThreadPoolTaskExecutor columnMappingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // Number of threads actively processing uploads
        executor.setMaxPoolSize(10);  // Max threads if more concurrent uploads occur
        executor.setQueueCapacity(50); // Number of files that can wait before execution
        executor.setThreadNamePrefix("columnMappingThread-");
        executor.initialize();
        return executor;
    }
}