package org.example.transactionservice.repository;

import org.example.transactionservice.model.ColumnMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ColumnMappingRepository extends JpaRepository<ColumnMapping, UUID> {
    Optional<ColumnMapping> findByFileId(UUID fileId);
}
