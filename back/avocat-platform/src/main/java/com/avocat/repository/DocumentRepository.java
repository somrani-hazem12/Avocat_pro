package com.avocat.repository;

import com.avocat.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByDossierId(Long dossierId);
    long countByDossierId(Long dossierId);
}