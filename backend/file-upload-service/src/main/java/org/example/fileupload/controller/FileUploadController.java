package org.example.fileupload.controller;

import org.example.fileupload.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Uploads a file and returns metadata.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("Received file upload request. File Name: {}, Size: {} MB",
                file.getOriginalFilename(), file.getSize() / (1024 * 1024));

        Map<String, Object> response = fileUploadService.uploadFile(file);

        logger.info("File upload response: {}", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves upload status for a given file ID.
     */
    @GetMapping("/status/{fileId}")
    public ResponseEntity<String> getFileUploadStatus(@PathVariable UUID fileId) {
        logger.info("Received status check request for File ID: {}", fileId);

        String status = fileUploadService.getFileStatus(fileId);

        logger.info("File status response: File ID: {}, Status: {}", fileId, status);
        return ResponseEntity.ok(status);
    }
}
