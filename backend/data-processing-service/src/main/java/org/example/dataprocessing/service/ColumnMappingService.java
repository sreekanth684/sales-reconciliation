package org.example.dataprocessing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.dataprocessing.messaging.CsvProcessingProducer;
import org.example.dataprocessing.messaging.TransactionMessageProducer;
import org.example.dataprocessing.model.ColumnMapping;
import org.example.dataprocessing.repository.ColumnMappingRepository;
import org.example.dataprocessing.repository.FileProcessingStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ColumnMappingService {

    private static final Logger logger = LoggerFactory.getLogger(ColumnMappingService.class);
    private final ColumnMappingRepository columnMappingRepository;
    private final FileProcessingStatusRepository fileProcessingStatusRepository;
    private final TransactionMessageProducer transactionMessageProducer;
    private final CsvProcessingProducer csvProcessingProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<UUID, String> fileProcessingStatusCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> fileErrorCountCache = new ConcurrentHashMap<>();
    @Value("${file.upload-dir}")
    private String uploadDir = "uploads/";

    public ColumnMappingService(ColumnMappingRepository columnMappingRepository,
                                FileProcessingStatusRepository fileProcessingStatusRepository,
                                TransactionMessageProducer transactionMessageProducer,
                                CsvProcessingProducer csvProcessingProducer) {
        this.columnMappingRepository = columnMappingRepository;
        this.fileProcessingStatusRepository = fileProcessingStatusRepository;
        this.transactionMessageProducer = transactionMessageProducer;
        this.csvProcessingProducer =csvProcessingProducer;
    }

    public void saveColumnMapping(UUID fileId, Map<String, String> mappings) {
        if (columnMappingRepository.findByFileId(fileId).isPresent()) {
            throw new IllegalArgumentException("Mapping already exists for this file.");
        }

        columnMappingRepository.save(new ColumnMapping(fileId, mappings));
        logger.info("Saved column mapping for file {}", fileId);
        startProcessing(fileId);
    }

    public void startProcessing(UUID fileId) {
        try{
            fileProcessingStatusCache.put(fileId, "PROCESSING");
            fileErrorCountCache.put(fileId, 0);
            logger.info("Processing started for file {}", fileId);

            updateProcessingStatus(fileId, "PROCESSING", 0, List.of());

            // Send file ID to RabbitMQ for async processing
            csvProcessingProducer.sendFileIdForProcessing(fileId);
        }catch (Exception e) {
            logger.error(" Error starting processing for file {}: {}", fileId, e.getMessage(), e);
            try {
                updateProcessingStatus(fileId, "FAILED", 0, List.of(" System error: " + e.getMessage()));
            } catch (Exception dbError) {
                logger.error(" Error updating status in catch block for file {}: {}", fileId, dbError.getMessage(), dbError);
            }
        }
        
    }

    public void processCsvFile(UUID fileId) {
        try{
            Optional<ColumnMapping> mappingOpt = columnMappingRepository.findByFileId(fileId);
            if (mappingOpt.isEmpty()) {
                logger.error(" No column mapping found for file {}", fileId);
                updateProcessingStatus(fileId, "FAILED", 0, List.of("No column mapping found."));
                return;
            }

            ColumnMapping mapping = mappingOpt.get();
            Path filePath = Path.of(uploadDir, fileId + ".csv");

            // Shared transaction ID set for global duplicate tracking across all records
            Set<String> transactionIds = ConcurrentHashMap.newKeySet();
            List<String> errors = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
                 CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                for (CSVRecord record : parser) {  // Reads and validates each row immediately
                    String error = validateRecord(record, mapping.getMappings(), transactionIds);
                    if (!error.isEmpty()) {
                        errors.add(error);
                    }
                }
            } catch (Exception e) {
                logger.error(" Error processing CSV file {}: {}", fileId, e.getMessage(), e);
                updateProcessingStatus(fileId, "FAILED", 0, List.of(e.getMessage()));
                return;
            }

            if (!errors.isEmpty()) {
                updateProcessingStatus(fileId, "FAILED", errors.size(), errors);
                return;
            }

            transactionMessageProducer.sendFileIdToTransactionService(fileId);
            updateProcessingStatus(fileId, "COMPLETED", 0, null);
        }catch (Exception e) {
            logger.error(" Critical error processing file {}: {}", fileId, e.getMessage(), e);

            try {
                updateProcessingStatus(fileId, "FAILED", 0, List.of(" System error: " + e.getMessage()));
            } catch (Exception dbError) {
                logger.error(" Error updating status in catch block for file {}: {}", fileId, dbError.getMessage(), dbError);
            }
        }

    }

    @Transactional
    private void updateProcessingStatus(UUID fileId, String status, int errorCount, List<String> errors) {
        try {
            String errorJson = errors != null ? objectMapper.writeValueAsString(errors) : "[]";

            fileProcessingStatusCache.put(fileId, status);
            fileErrorCountCache.put(fileId, errorCount);

            fileProcessingStatusRepository.updateProcessingStatus(fileId, status, errorCount, errorJson);
            logger.info(" Successfully updated processing status for file {}", fileId);
        } catch (Exception e) {
            logger.error(" Error updating processing status for file {}: {}", fileId, e.getMessage(), e);
            throw new RuntimeException(" Failed to update processing status for file: " + fileId, e);
        }
    }


    public Map<String, Object> getProcessingStatus(UUID fileId) {
        if (fileProcessingStatusCache.containsKey(fileId)) {
            return Map.of(
                    "fileId", fileId,
                    "status", fileProcessingStatusCache.get(fileId),
                    "errorCount", fileErrorCountCache.getOrDefault(fileId, 0),
                    "errorUrl", "/api/mapping/errors/" + fileId
            );
        }

        Optional<Object[]> result = fileProcessingStatusRepository.findProcessingStatus(fileId);
        if (result.isEmpty()) {
            return Map.of("fileId", fileId, "status", "NOT_FOUND");
        }

        String status = (String) result.get()[0];
        int errorCount = (int) result.get()[1];

        fileProcessingStatusCache.put(fileId, status);
        fileErrorCountCache.put(fileId, errorCount);

        return Map.of(
                "fileId", fileId,
                "status", status,
                "errorCount", errorCount,
                "errorUrl", "/api/mapping/errors/" + fileId
        );
    }

    public List<String> getValidationErrors(UUID fileId, int page, int size) {
        Optional<String> errorsJson = fileProcessingStatusRepository.findValidationErrors(fileId);
        if (errorsJson.isEmpty()) return List.of("No errors found.");

        try {
            List<String> allErrors = objectMapper.readValue(errorsJson.get(), List.class);
            int start = Math.min((page - 1) * size, allErrors.size());
            int end = Math.min(start + size, allErrors.size());

            return allErrors.subList(start, end);
        } catch (Exception e) {
            logger.error(" Error retrieving validation errors: {}", e.getMessage(), e);
            return List.of("Error retrieving errors.");
        }
    }

    private String validateRecord(CSVRecord record, Map<String, String> mappings, Set<String> transactionIds) {
        StringBuilder error = new StringBuilder();

        if (mappings.containsKey("TransactionID")) {
            String transactionId = record.get(mappings.get("TransactionID"));
            if (transactionId.isEmpty() || !transactionIds.add(transactionId)) {
                error.append("Row ").append(record.getRecordNumber()).append(": Duplicate or missing TransactionID. ");
            }
        }

        if (mappings.containsKey("TransactionDate")) {
            String date = record.get(mappings.get("TransactionDate"));
            if (!Pattern.matches("\\d{4}-\\d{2}-\\d{2}", date)) {
                error.append("Row ").append(record.getRecordNumber()).append(": Invalid date format. ");
            }
        }

        return error.toString().trim();
    }
}
