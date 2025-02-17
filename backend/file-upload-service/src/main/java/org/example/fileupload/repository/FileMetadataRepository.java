package org.example.fileupload.repository;

import org.example.fileupload.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    // Find all versions of a file (oldest to newest) by original filename
    List<FileMetadata> findByOriginalFilenameOrderByUploadTimestampAsc(String originalFilename);

    // Find the latest uploaded version of a file
    Optional<FileMetadata> findFirstByOriginalFilenameOrderByUploadTimestampDesc(String originalFilename);

    // Find by fileId (UUID) for status retrieval
    Optional<FileMetadata> findByFileId(UUID fileId);

    // Find all files by status (e.g., "PROCESSING", "FAILED")
    List<FileMetadata> findByUploadStatus(String uploadStatus);
}
