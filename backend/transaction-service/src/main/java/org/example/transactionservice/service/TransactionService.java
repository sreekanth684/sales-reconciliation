package org.example.transactionservice.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.transactionservice.model.ColumnMapping;
import org.example.transactionservice.model.Transaction;
import org.example.transactionservice.repository.ColumnMappingRepository;
import org.example.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    private final ColumnMappingRepository columnMappingRepository;
    @Value("${file.upload-dir}")
    private String uploadDir = "uploads/";
    private static final int BATCH_SIZE = 1000;

    public TransactionService(TransactionRepository transactionRepository, ColumnMappingRepository columnMappingRepository) {
        this.transactionRepository = transactionRepository;
        this.columnMappingRepository = columnMappingRepository;
    }

    /**
     * Processes a file by retrieving mappings and inserting transactions in batches.
     */
    public void processFile(UUID fileId) {
        try {
            logger.info("📥 Processing transactions for file {}", fileId);

            //  Retrieve column mappings dynamically from the database
            Optional<ColumnMapping> mappingOpt = columnMappingRepository.findByFileId(fileId);
            if (mappingOpt.isEmpty()) {
                logger.error(" No column mapping found for file {}", fileId);
                return;
            }
            ColumnMapping mapping = mappingOpt.get();
            Map<String, String> columnMappings = mapping.getMappings();  //  Dynamically retrieved mappings

            Path filePath = Path.of(uploadDir, fileId + ".csv");
            List<Transaction> transactionBatch = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
                 CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                for (CSVRecord record : parser) {
                    String transactionId = null;
                    LocalDate transactionDate = null;
                    BigDecimal amount = null;
                    String customerName = null;
                    String paymentMethod = null;
                    String shippingAddressCity = null;

                    //  Iterate through columnMappings dynamically
                    for (Map.Entry<String, String> entry : columnMappings.entrySet()) {
                        String csvHeader = entry.getKey();   //  CSV column name
                        String dbColumn = entry.getValue();  //  Database column name

                        String value = record.get(csvHeader);  //  Extract value from CSV

                        //  Dynamically map values to Transaction fields
                        if ("transaction_id".equals(dbColumn)) {
                            transactionId = value;
                        } else if ("transaction_date".equals(dbColumn)) {
                            transactionDate = LocalDate.parse(value);
                        } else if ("amount".equals(dbColumn)) {
                            amount = new BigDecimal(value);
                        } else if ("customer_name".equals(dbColumn)) {
                            customerName = value;
                        } else if ("payment_method".equals(dbColumn)) {
                            paymentMethod = value;
                        } else if ("shipping_address_city".equals(dbColumn)) {
                            shippingAddressCity = value;
                        }
                    }

                    //  Ensure required fields are not null
                    if (transactionId != null && transactionDate != null && amount != null &&
                            customerName != null && paymentMethod != null && shippingAddressCity != null) {
                        Transaction transaction = new Transaction(
                                fileId, transactionId, transactionDate, amount, customerName, paymentMethod, shippingAddressCity
                        );
                        transactionBatch.add(transaction);
                    }

                    if (transactionBatch.size() >= BATCH_SIZE) {
                        transactionRepository.saveAll(transactionBatch);
                        logger.info(" Inserted batch of {} transactions for file {}", BATCH_SIZE, fileId);
                        transactionBatch.clear();
                    }
                }
            }

            if (!transactionBatch.isEmpty()) {
                transactionRepository.saveAll(transactionBatch);
                logger.info(" Inserted final batch of {} transactions for file {}", transactionBatch.size(), fileId);
            }

            logger.info(" Successfully processed all transactions for file {}", fileId);

        } catch (Exception e) {
            logger.error(" Error processing file {}: {}", fileId, e.getMessage(), e);
        }
    }


}
