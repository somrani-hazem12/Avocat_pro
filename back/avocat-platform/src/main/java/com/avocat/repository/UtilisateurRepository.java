package com.avocat.repository;

import com.avocat.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findByRole(String role);
    List<Utilisateur> findByStatut(String statut);
    long countByStatut(String statut);

    // NOUVELLE MÉTHODE : Compter par rôle (AVOCAT, CLIENT, etc.)
    long countByRole(String role);
}