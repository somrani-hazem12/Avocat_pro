package com.avocat.repository;

import com.avocat.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByDateActionDesc();
}