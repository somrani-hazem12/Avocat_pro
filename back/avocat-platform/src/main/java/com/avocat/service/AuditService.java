package com.avocat.service;

import com.avocat.entity.AuditLog;
import com.avocat.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    public void enregistrerLog(String action, String cible, String details) {
        // Récupérer l'utilisateur connecté dynamiquement depuis Spring Security
        String emailActuel = SecurityContextHolder.getContext().getAuthentication().getName();

        AuditLog log = AuditLog.builder()
                .action(action)
                .utilisateur(emailActuel)
                .cible(cible)
                .details(details)
                .adresseIp(request.getRemoteAddr())
                .dateAction(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    public List<AuditLog> recupererTousLesLogs() {
        return auditLogRepository.findAllByOrderByDateActionDesc();
    }
}