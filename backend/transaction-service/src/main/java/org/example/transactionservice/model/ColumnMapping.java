package org.example.transactionservice.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "column_mapping")
public class ColumnMapping {

    @Id
    private UUID fileId;

    @Column(columnDefinition = "TEXT")  // Stores mappings in JSON format
    private String mappingsJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Transient // Prevents Hibernate from mapping it to a DB column
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ColumnMapping() {
        this.createdAt = Instant.now();
    }

    public ColumnMapping(UUID fileId, Map<String, String> mappings) {
        this.fileId = fileId;
        this.mappingsJson = convertMapToJson(mappings);
        this.createdAt = Instant.now();
    }

    public UUID getFileId() { return fileId; }

    public Map<String, String> getMappings() {
        return convertJsonToMap(mappingsJson);
    }

    public void setMappings(Map<String, String> mappings) {
        this.mappingsJson = convertMapToJson(mappings);
    }

    public Instant getCreatedAt() { return createdAt; }

    private String convertMapToJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert Map to JSON", e);
        }
    }

    private Map<String, String> convertJsonToMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to Map", e);
        }
    }
}
