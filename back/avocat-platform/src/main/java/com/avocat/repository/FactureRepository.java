package com.avocat.repository;

import com.avocat.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    // L'avocat voit les factures qu'il a générées
    List<Facture> findByUtilisateurId(Long utilisateurId);

    // Le client voit les factures qui lui sont adressées
    // Note : On utilise l'ID de l'entité Client (ou Utilisateur avec role Client)
    List<Facture> findByClientId(Long clientId);
}