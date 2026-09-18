package com.avocat.repository;

import com.avocat.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    // Cette méthode permet de trouver le profil Client à partir de l'Utilisateur connecté
    Optional<Client> findByUtilisateurId(Long utilisateurId);
}