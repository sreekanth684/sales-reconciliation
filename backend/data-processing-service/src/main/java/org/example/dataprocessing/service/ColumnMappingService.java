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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ColumnMappingService {

    private static final Logger logger = LoggerFactory.getLogger(ColumnMappingService.class);
    @Value("${file.upload-dir}")
    private String uploadDir = "uploads/";
    private final ColumnMappingRepository columnMappingRepository;
    private final FileProcessingStatusRepository fileProcessingStatusRepository;
    private final TransactionMessageProducer transactionMessageProducer;

    private CsvProcessingProducer csvProcessingProducer;

    private final ThreadPoolTaskExecutor columnMappingExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<UUID, String> fileProcessingStatusCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> fileErrorCountCache = new ConcurrentHashMap<>();

    public ColumnMappingService(ColumnMappingRepository columnMappingRepository,
                                FileProcessingStatusRepository fileProcessingStatusRepository,
                                TransactionMessageProducer transactionMessageProducer,
                                CsvProcessingProducer csvProcessingProducer,
                                ThreadPoolTaskExecutor columnMappingExecutor) {
        this.columnMappingRepository = columnMappingRepository;
        this.fileProcessingStatusRepository = fileProcessingStatusRepository;
        this.transactionMessageProducer = transactionMessageProducer;
        this.columnMappingExecutor = columnMappingExecutor;
        this.csvProcessingProducer = csvProcessingProducer;
    }

    public void saveColumnMapping(UUID fileId, Map<String, String> mappings) {
        if (columnMappingRepository.findByFileId(fileId).isPresent()) {
            throw new IllegalArgumentException("Mapping already exists for this file.");
        }

        columnMappingRepository.save(new ColumnMapping(fileId, mappings));
        logger.info("Saved column mapping for file {}", fileId);
        startProcessing(fileId);
    }

//    public void startProcessing(UUID fileId) {
//        fileProcessingStatusCache.put(fileId, "PROCESSING");
//        fileErrorCountCache.put(fileId, 0);
//        logger.info("CSV processing started for file {}", fileId);
//
//        CompletableFuture.runAsync(() -> processCsvFile(fileId), columnMappingExecutor)
//                .exceptionally(ex -> {
//                    logger.error(" Error processing file {}: {}", fileId, ex.getMessage(), ex);
//                    updateProcessingStatus(fileId, "FAILED", 0, List.of(ex.getMessage()));
//                    return null;
//                });
//    }
    public void startProcessing(UUID fileId) {
        fileProcessingStatusCache.put(fileId, "PROCESSING");
        fileErrorCountCache.put(fileId, 0);
        logger.info("Queuing file {} for processing via RabbitMQ", fileId);
        updateProcessingStatus(fileId, "PROCESSING", 0, List.of());

        csvProcessingProducer.sendFileIdForProcessing(fileId);
    }

    public void processCsvFile(UUID fileId) {
        try {
            Optional<ColumnMapping> mappingOpt = columnMappingRepository.findByFileId(fileId);
            if (mappingOpt.isEmpty()) {
                logger.error(" No column mapping found for file {}", fileId);
                updateProcessingStatus(fileId, "FAILED", 0, List.of("No column mapping found."));
                return;
            }

            ColumnMapping mapping = mappingOpt.get();
            Path filePath = Path.of(uploadDir, fileId + ".csv");

            List<CSVRecord> records;
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
                 CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                records = parser.getRecords();
            } catch (Exception e) {
                logger.error(" Error processing CSV file {}: {}", fileId, e.getMessage(), e);
                updateProcessingStatus(fileId, "FAILED", 0, List.of(e.getMessage()));
                return;
            }

            List<String> errors = validateRecordsInParallel(records, mapping.getMappings());

            if (!errors.isEmpty()) {
                updateProcessingStatus(fileId, "FAILED", errors.size(), errors);
                return;
            }

            transactionMessageProducer.sendFileIdToTransactionService(fileId);
            updateProcessingStatus(fileId, "COMPLETED", 0, null);
        } catch (Exception e) {
            logger.error("Unexpected error processing file {}: {}", fileId, e.getMessage(), e);
            updateProcessingStatus(fileId, "FAILED", 0, List.of("Unexpected processing error: " + e.getMessage()));
        }
    }

    @Transactional
    private void updateProcessingStatus(UUID fileId, String status, int errorCount, List<String> errors) {
        try {
            String errorJson = errors != null ? objectMapper.writeValueAsString(errors) : "[]";

            fileProcessingStatusCache.put(fileId, status);
            fileErrorCountCache.put(fileId, errorCount);

            fileProcessingStatusRepository.updateProcessingStatus(fileId, status, errorCount, errorJson);
        } catch (Exception e) {
            logger.error(" Error updating processing status: {}", e.getMessage(), e);
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

    private List<String> validateRecordsInParallel(List<CSVRecord> records, Map<String, String> mappings) {
        int batchSize = 10000;
        int totalBatches = (int) Math.ceil((double) records.size() / batchSize);

        return IntStream.range(0, totalBatches)
                .parallel()
                .mapToObj(batchIndex -> records.subList(batchIndex * batchSize,
                                Math.min((batchIndex + 1) * batchSize, records.size()))
                        .stream()
                        .map(record -> validateRecord(record, mappings, new HashSet<>()))
                        .filter(error -> !error.isEmpty())
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private String validateRecord(CSVRecord record, Map<String, String> mappings, Set<String> transactionIds) {
        StringBuilder error = new StringBuilder();

        if (mappings.containsKey("TransactionID")) {
            String transactionId = record.get(mappings.get("TransactionID"));
            if (transactionId.isEmpty() || !transactionIds.add(transactionId)) {
                error.append("Row ").append(record.getRecordNumber()).append(": Invalid or duplicate TransactionID. ");
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
