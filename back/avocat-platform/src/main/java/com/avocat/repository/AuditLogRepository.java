package com.avocat.repository;

import com.avocat.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Récupère les logs du plus récent au plus ancien
    List<AuditLog> findAllByOrderByDateActionDesc();
}