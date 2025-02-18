package org.example.dataprocessing.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue csvProcessingQueue() {
        return new Queue("csv-processing-queue", true);
    }

    @Bean
    public Queue transactionProcessingQueue() {
        return new Queue("transaction-processing-queue", true);
    }
}
