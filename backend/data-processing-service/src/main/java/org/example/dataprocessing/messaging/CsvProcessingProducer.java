package org.example.dataprocessing.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CsvProcessingProducer {

    private static final Logger logger = LoggerFactory.getLogger(CsvProcessingProducer.class);
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.csv-processing}")
    private String csvProcessingQueue;

    public CsvProcessingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Sends fileId to RabbitMQ for async processing.
     */
    public void sendFileIdForProcessing(UUID fileId) {
        logger.info("📤 Sending fileId {} to CSV processing queue", fileId);
        rabbitTemplate.convertAndSend(csvProcessingQueue, fileId.toString());
    }
}
