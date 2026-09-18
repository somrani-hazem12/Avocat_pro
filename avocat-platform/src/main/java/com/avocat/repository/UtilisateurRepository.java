package com.avocat.repository;

import com.avocat.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Trouver par email
    Optional<Utilisateur> findByEmail(String email);

    // Trouver par rôle (ex: AVOCAT, ADMINISTRATEUR, CLIENT)
    List<Utilisateur> findAllByRole(String role);

    // Trouver par statut (ex: ACTIF, EN_ATTENTE, REFUSE)
    List<Utilisateur> findByStatut(String statut);

    // Alternative avec @Query si nécessaire
    @Query("SELECT u FROM Utilisateur u WHERE u.role = :role")
    List<Utilisateur> getByRole(@Param("role") String role);
}