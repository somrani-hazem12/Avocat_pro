package com.avocat.repository;

import com.avocat.entity.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    // Liste pour l'avocat (par son ID utilisateur)
    List<RendezVous> findByUtilisateurId(Long utilisateurId);

    // Liste pour le client (via l'ID de l'utilisateur lié au profil client)
    List<RendezVous> findByClientUtilisateurId(Long utilisateurId);
    // On compte les RDV avec le statut 'DEMANDE' pour cet avocat
    long countByUtilisateurIdAndStatut(Long avocatId, String statut);
}
