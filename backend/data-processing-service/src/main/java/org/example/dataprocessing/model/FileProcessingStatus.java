package org.example.dataprocessing.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_processing_status")
public class FileProcessingStatus {

    @Id
    private UUID fileId;

    @Column(nullable = false)
    private String status; // PROCESSING, FAILED, COMPLETED

    @Column(nullable = false)
    private int errorCount = 0;

    @Column(columnDefinition = "TEXT") // JSON string to store validation errors
    private String validationErrors = "[]";

    @Column(nullable = false, updatable = false)
    private Instant processingStart = Instant.now();

    private Instant processingEnd;

    public FileProcessingStatus() {}

    public FileProcessingStatus(UUID fileId, String status, int errorCount, String validationErrors) {
        this.fileId = fileId;
        this.status = status;
        this.errorCount = errorCount;
        this.validationErrors = validationErrors;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public String getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(String validationErrors) {
        this.validationErrors = validationErrors;
    }

    public Instant getProcessingStart() {
        return processingStart;
    }

    public void setProcessingStart(Instant processingStart) {
        this.processingStart = processingStart;
    }

    public Instant getProcessingEnd() {
        return processingEnd;
    }

    public void setProcessingEnd(Instant processingEnd) {
        this.processingEnd = processingEnd;
    }
}
