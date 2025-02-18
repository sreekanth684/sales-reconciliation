package org.example.dataprocessing.repository;

import org.example.dataprocessing.model.FileProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileProcessingStatusRepository extends JpaRepository<FileProcessingStatus, UUID> {

    /**
     * Updates or inserts processing status and validation errors in DB.
     */
    @Modifying
    @Transactional
    @Query(value = """
    INSERT INTO file_processing_status (file_id, status, error_count, validation_errors, processing_start, processing_end) 
    VALUES (:fileId, :status, :errorCount, :errors, NOW(), 
        CASE WHEN :status = 'COMPLETED' OR :status = 'FAILED' THEN NOW() ELSE NULL END) 
    ON CONFLICT (file_id) 
    DO UPDATE SET 
    status = EXCLUDED.status, 
    error_count = EXCLUDED.error_count, 
    validation_errors = EXCLUDED.validation_errors, 
    processing_end = CASE 
        WHEN EXCLUDED.status = 'COMPLETED' OR EXCLUDED.status = 'FAILED' THEN NOW() 
        ELSE file_processing_status.processing_end 
    END
    """, nativeQuery = true)
    void updateProcessingStatus(@Param("fileId") UUID fileId,
                                @Param("status") String status,
                                @Param("errorCount") int errorCount,
                                @Param("errors") String errors);



    /**
     * Retrieves file processing status and error count.
     */
    @Query(value = "SELECT status, error_count FROM file_processing_status WHERE file_id = :fileId", nativeQuery = true)
    Optional<Object[]> findProcessingStatus(@Param("fileId") UUID fileId);

    /**
     * Retrieves full validation errors for a given file.
     */
    @Query(value = "SELECT validation_errors FROM file_processing_status WHERE file_id = :fileId", nativeQuery = true)
    Optional<String> findValidationErrors(@Param("fileId") UUID fileId);
}
