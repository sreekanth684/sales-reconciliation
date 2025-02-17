package org.example.fileupload.service;

import org.example.fileupload.model.FileMetadata;
import org.example.fileupload.repository.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    private static final String UPLOAD_DIR = "uploads/";
    private static final long ASYNC_THRESHOLD = 10 * 1024 * 1024; // 10MB threshold for async storage

    private final FileMetadataRepository fileMetadataRepository;
    private final ThreadPoolTaskExecutor fileUploadExecutor;
    private final Map<UUID, String> fileStatusCache = new ConcurrentHashMap<>(); // FileID -> Status Cache

    public FileUploadService(FileMetadataRepository fileMetadataRepository, ThreadPoolTaskExecutor fileUploadExecutor) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileUploadExecutor = fileUploadExecutor;
    }

    /**
     * Handles file upload and tracking.
     */
    public Map<String, Object> uploadFile(MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        String originalFilename = file.getOriginalFilename();
        String sanitizedFilename = sanitizeFilename(originalFilename);
        Path storagePath = Path.of(UPLOAD_DIR, sanitizedFilename);

        logger.info("Uploading file: {} (Sanitized: {})", originalFilename, sanitizedFilename);

        try {
            // Create Metadata Record in Database (JPA will generate fileId)
            FileMetadata savedMetadata = fileMetadataRepository.save(new FileMetadata(originalFilename, storagePath.toString(), "PENDING"));
            UUID fileId = savedMetadata.getFileId();
            fileStatusCache.put(fileId, "PENDING");

            response.put("fileId", fileId);
            response.put("originalFilename", originalFilename);
            response.put("sanitizedFilename", sanitizedFilename);

            // Store file based on size
            if (file.getSize() > ASYNC_THRESHOLD) {
                storeFileAsync(file, savedMetadata); //  Async processing with pre-loaded file bytes
                response.put("message", "File upload started asynchronously.");
            } else {
                boolean success = storeFileSync(file, savedMetadata);
                response.put("message", success ? "File uploaded successfully." : "File upload failed. Check logs for details.");
            }

        } catch (Exception e) { 
            logger.error("File upload failed for file: {} - {}", originalFilename, e.getMessage(), e);
            response.put("message", "File upload failed: " + e.getMessage());
        }

        return response;
    }

    /**
     * Synchronous file storage.
     */
    private boolean storeFileSync(MultipartFile file, FileMetadata metadata) {
        try {
            Path filePath = Path.of(metadata.getStoragePath());
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING); //  Directly store synchronously

            updateStatus(metadata, "UPLOADED", null);
            logger.info("File stored successfully: {}", metadata.getStoragePath());
            return true;
        } catch (Exception e) { 
            updateStatus(metadata, "FAILED", e.getMessage());
            logger.error("File storage failed for file {}: {}", metadata.getStoragePath(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Asynchronous file storage (Reads file into memory before processing).
     */
    private void storeFileAsync(MultipartFile file, FileMetadata metadata) {
        try {
            updateStatus(metadata, "PROCESSING", null);
            metadata.setProcessingStartTime(Instant.now());
            fileMetadataRepository.save(metadata);

            //  Read file into memory before async processing (Fix for temp file deletion issue)
            byte[] fileBytes = file.getBytes();

            CompletableFuture.runAsync(() -> {
                try {
                    Path filePath = Path.of(metadata.getStoragePath());
                    Files.write(filePath, fileBytes); //  Save the file from memory

                    metadata.setProcessingEndTime(Instant.now());
                    updateStatus(metadata, "UPLOADED", null);
                    logger.info("File stored asynchronously: {}", metadata.getStoragePath());
                } catch (Exception e) { 
                    updateStatus(metadata, "FAILED", e.getMessage());
                    logger.error("Async file storage failed for file {}: {}", metadata.getStoragePath(), e.getMessage(), e);
                }
            }, fileUploadExecutor);

        } catch (Exception e) { 
            updateStatus(metadata, "FAILED", e.getMessage());
            logger.error("Failed to read file into memory for async processing: {}", e.getMessage(), e);
        }
    }

    /**
     * Updates file status in DB & cache.
     */
    private void updateStatus(FileMetadata metadata, String status, String errorMessage) {
        metadata.setUploadStatus(status);
        metadata.setErrorMessage(errorMessage);
        fileMetadataRepository.save(metadata);
        fileStatusCache.put(metadata.getFileId(), status);
        logger.info("Updated status for file {}: {}", metadata.getOriginalFilename(), status);
    }

    /**
     * Retrieves file status from cache first, then from DB if needed.
     */
    public String getFileStatus(UUID fileId) {
        return fileStatusCache.computeIfAbsent(fileId, id ->
                fileMetadataRepository.findByFileId(id)
                        .map(FileMetadata::getUploadStatus)
                        .orElse("UNKNOWN")
        );
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9._-]", "");
    }
}
