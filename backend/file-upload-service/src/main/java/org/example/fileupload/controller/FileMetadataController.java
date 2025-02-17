package org.example.fileupload.controller;

import org.example.fileupload.model.FileMetadata;
import org.example.fileupload.repository.FileMetadataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileMetadataController {

    private final FileMetadataRepository fileMetadataRepository;

    public FileMetadataController(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    /**
     * Fetches the latest version of a file by its original filename.
     */
    @GetMapping("/latest/{filename}")
    public ResponseEntity<?> getLatestFileVersion(@PathVariable String filename) {
        return fileMetadataRepository.findFirstByOriginalFilenameOrderByUploadTimestampDesc(filename)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Fetches metadata for a file by its fileId.
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<?> getFileMetadata(@PathVariable UUID fileId) {
        Optional<FileMetadata> fileMetadata = fileMetadataRepository.findByFileId(fileId);
        return fileMetadata.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
