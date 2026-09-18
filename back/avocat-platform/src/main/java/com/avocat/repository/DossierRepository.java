package com.avocat.repository;

import com.avocat.entity.Dossier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DossierRepository extends JpaRepository<Dossier, Long> {

    // CORRECTION : On retire 'static' et les '{ }'.
    // Spring va générer automatiquement : SELECT COUNT(*) FROM dossiers WHERE utilisateur_id = ?
    long countByUtilisateurId(Long utilisateurId);

    // Pour lister tous les dossiers d'un avocat spécifique
    List<Dossier> findByUtilisateurId(Long utilisateurId);

    // Pour lister tous les dossiers d'un client spécifique (utile pour l'interface client)
    List<Dossier> findByClientId(Long clientId);

    List<Dossier> findByClient_Utilisateur_Id(Long utilisateurId);
    List<Dossier> findByUtilisateurIdAndClient_Utilisateur_Id(Long lawyerId, Long clientUserId);



}