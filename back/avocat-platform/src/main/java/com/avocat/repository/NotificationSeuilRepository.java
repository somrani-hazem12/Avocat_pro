package com.avocat.repository;

import com.avocat.entity.NotificationSeuil;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationSeuilRepository
        extends JpaRepository<NotificationSeuil, Long> {
    Optional<NotificationSeuil> findByUtilisateurId(Long utilisateurId);
}