package com.avocat.repository;

import com.avocat.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {
    List<Notification> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);
    List<Notification> findByUtilisateurIdAndLueFalse(Long utilisateurId);
    long countByUtilisateurIdAndLueFalse(Long utilisateurId);
}