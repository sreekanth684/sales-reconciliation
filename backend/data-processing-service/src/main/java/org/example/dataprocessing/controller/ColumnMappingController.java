package org.example.dataprocessing.controller;

import org.example.dataprocessing.service.ColumnMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/mapping")
public class ColumnMappingController {

    private static final Logger logger = LoggerFactory.getLogger(ColumnMappingController.class);
    private final ColumnMappingService columnMappingService;

    public ColumnMappingController(ColumnMappingService columnMappingService) {
        this.columnMappingService = columnMappingService;
    }

    /**
     * Saves column mapping and starts CSV processing.
     */
    @PostMapping
    public ResponseEntity<String> saveMapping(@RequestBody Map<String, Object> request) {
        try {
            UUID fileId = UUID.fromString(request.get("fileId").toString());
            Map<String, String> mappings = (Map<String, String>) request.get("mappings");

            if (fileId == null || mappings == null || mappings.isEmpty()) {
                return ResponseEntity.badRequest().body(" Invalid request: fileId and mappings are required.");
            }

            columnMappingService.saveColumnMapping(fileId, mappings);
            return ResponseEntity.ok("Mapping saved successfully. Processing started.");
        } catch (Exception e) {
            logger.error(" Error saving mapping: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(" Error saving mapping: " + e.getMessage());
        }
    }

    /**
     * Retrieves processing status (status & error count only).
     */
    @GetMapping("/status/{fileId}")
    public ResponseEntity<Map<String, Object>> getProcessingStatus(@PathVariable UUID fileId) {
        return ResponseEntity.ok(columnMappingService.getProcessingStatus(fileId));
    }

    /**
     * Retrieves validation errors (paginated).
     */
    @GetMapping("/errors/{fileId}")
    public ResponseEntity<?> getValidationErrors(
            @PathVariable UUID fileId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(columnMappingService.getValidationErrors(fileId, page, size));
        } catch (Exception e) {
            logger.error(" Error retrieving validation errors for file {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(" Error retrieving errors.");
        }
    }
}
