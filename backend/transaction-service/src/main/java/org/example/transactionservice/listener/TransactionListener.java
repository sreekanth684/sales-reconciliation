package org.example.transactionservice.listener;

import org.example.transactionservice.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);
    private final TransactionService transactionService;

    public TransactionListener(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @RabbitListener(queues = "transaction-processing-queue", concurrency = "3-5")
    public void processFile(String fileIdStr) {
        UUID fileId = UUID.fromString(fileIdStr);
        logger.info("📥 Received fileId {} for processing", fileId);
        transactionService.processFile(fileId);
    }
}
