package org.example.fileupload.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "file_id", updatable = false, nullable = false)
    private UUID fileId; // Primary Key (UUID)

    @Column(name = "original_filename", nullable = false)
    private String originalFilename; // User-uploaded filename

    @Column(name = "storage_path", nullable = false)
    private String storagePath; // Actual file storage location

    @Column(name = "upload_timestamp", nullable = false)
    private Instant uploadTimestamp; // When file was uploaded

    @Column(name = "upload_status", nullable = false)
    private String uploadStatus; // Status: PENDING, PROCESSING, UPLOADED, FAILED

    @Column(name = "processing_start_time")
    private Instant processingStartTime; // When processing started (nullable)

    @Column(name = "processing_end_time")
    private Instant processingEndTime; // When processing ended (nullable)

    @Column(name = "error_message")
    private String errorMessage; // Stores error details if processing fails (nullable)

    // Default Constructor (for JPA)
    public FileMetadata() {}

    // Constructor for new uploads
    public FileMetadata(String originalFilename, String storagePath, String uploadStatus) {
        this.originalFilename = originalFilename;
        this.storagePath = storagePath;
        this.uploadTimestamp = Instant.now();
        this.uploadStatus = uploadStatus;
    }

    // Getters & Setters
    public UUID getFileId() { return fileId; }
    public void setFileId(UUID fileId) { this.fileId = fileId; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public Instant getUploadTimestamp() { return uploadTimestamp; }
    public void setUploadTimestamp(Instant uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }

    public String getUploadStatus() { return uploadStatus; }
    public void setUploadStatus(String uploadStatus) { this.uploadStatus = uploadStatus; }

    public Instant getProcessingStartTime() { return processingStartTime; }
    public void setProcessingStartTime(Instant processingStartTime) { this.processingStartTime = processingStartTime; }

    public Instant getProcessingEndTime() { return processingEndTime; }
    public void setProcessingEndTime(Instant processingEndTime) { this.processingEndTime = processingEndTime; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
