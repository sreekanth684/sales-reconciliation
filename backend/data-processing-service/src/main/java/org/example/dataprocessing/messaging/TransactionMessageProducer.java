package org.example.dataprocessing.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(TransactionMessageProducer.class);
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.transaction-processing}")
    private String transactionQueue;

    public TransactionMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Sends fileId to transaction-service via RabbitMQ.
     */
    public void sendFileIdToTransactionService(UUID fileId) {
        logger.info("Sending fileId {} to transaction-service queue", fileId);
        rabbitTemplate.convertAndSend(transactionQueue, fileId.toString());
    }
}
