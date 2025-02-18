package org.example.dataprocessing.listener;

import org.example.dataprocessing.service.ColumnMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CsvProcessingListener {

    private static final Logger logger = LoggerFactory.getLogger(CsvProcessingListener.class);
    private final ColumnMappingService columnMappingService;

    public CsvProcessingListener(ColumnMappingService columnMappingService) {
        this.columnMappingService = columnMappingService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.csv-processing}")
    public void processCsvMessage(String fileId) {
        try {
            logger.info("📥 Received file {} for processing via RabbitMQ", fileId);
            columnMappingService.processCsvFile(UUID.fromString(fileId));
        } catch (Exception e) {
            logger.error(" Error processing file {}: {}", fileId, e.getMessage(), e);
        }
    }
}
