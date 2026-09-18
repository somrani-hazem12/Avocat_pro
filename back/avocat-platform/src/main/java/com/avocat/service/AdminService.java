package com.avocat.service;

import com.avocat.entity.Utilisateur;
import com.avocat.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AuditService auditService; // AJOUT ICI
    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;

    private final ClientRepository clientRepository;
    private final SuiviAvocatRepository suiviAvocatRepository;
    private final DossierRepository dossierRepository;
    private final RendezVousRepository rendezvousRepository;
    private final FactureRepository factureRepository;

    public List<Utilisateur> getAllUsers() {
        return utilisateurRepository.findAll();
    }

    public List<Utilisateur> getInscriptionsEnAttente() {
        return utilisateurRepository.findByStatut("EN_ATTENTE");
    }

    public void accepterUtilisateur(Long id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setStatut("ACTIF");
        utilisateurRepository.save(user);
        auditService.enregistrerLog("VALIDATION_INSCRIPTION", user.getEmail(), "Compte activé par l'administrateur");
        String contenu = "Bonjour " + user.getNom() + ",\n\n" +
                "Votre compte sur Avocat Pro a été validé avec succès.\n" +
                "Vous pouvez maintenant vous connecter.\n\n" +
                "Cordialement,\nL'équipe Avocat Pro";

        emailService.envoyerEmail(user.getEmail(), "Compte Activé - Avocat Pro", contenu);
    }

    public void refuserUtilisateur(Long id, String motif) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setStatut("REFUSE");
        user.setMotifRefus(motif);
        utilisateurRepository.save(user);
        auditService.enregistrerLog("REFUS_INSCRIPTION", user.getEmail(), "Motif : " + motif);
        String contenu = "Bonjour " + user.getNom() + ",\n\n" +
                "Votre demande d'inscription a été refusée.\n" +
                "Motif : " + motif + "\n\n" +
                "L'équipe Avocat Pro";

        emailService.envoyerEmail(user.getEmail(), "Mise à jour de votre inscription", contenu);
    }

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", utilisateurRepository.count());
        stats.put("enAttente", utilisateurRepository.countByStatut("EN_ATTENTE"));
        stats.put("actifs", utilisateurRepository.countByStatut("ACTIF"));

        // AJOUT DES STATS PAR RÔLE
        stats.put("avocats", utilisateurRepository.countByRole("AVOCAT"));
        stats.put("clients", utilisateurRepository.countByRole("CLIENT"));

        return stats;
    }


    @Transactional
    public void deleteUser(Long id) {
        Utilisateur user = utilisateurRepository.findById(id).orElseThrow();
        String emailCible = user.getEmail();


        // 1. Supprimer les liens de partenariat (SuiviAvocat)
        suiviAvocatRepository.findByClientId(id).forEach(suiviAvocatRepository::delete);
        suiviAvocatRepository.findByAvocatId(id).forEach(suiviAvocatRepository::delete);

        // 2. Supprimer les Rendez-vous
        rendezvousRepository.findByUtilisateurId(id).forEach(rendezvousRepository::delete);
        rendezvousRepository.findByClientUtilisateurId(id).forEach(rendezvousRepository::delete);

        // 3. Supprimer les Dossiers (Attention : gère aussi les documents si nécessaire)
        dossierRepository.findByUtilisateurId(id).forEach(dossierRepository::delete);
        dossierRepository.findByClient_Utilisateur_Id(id).forEach(dossierRepository::delete);

        // 4. Supprimer les Factures
        factureRepository.findByUtilisateurId(id).forEach(factureRepository::delete);

        // 5. Supprimer le profil dans la table 'clients' si c'est un client
        clientRepository.findByUtilisateurId(id).ifPresent(clientRepository::delete);

        // 6. Enfin, supprimer le compte Utilisateur
        utilisateurRepository.delete(user);
        auditService.enregistrerLog("SUPPRESSION_COMPTE", emailCible, "Suppression définitive de l'utilisateur et de ses données");
    }

    // Dans AdminService.java
    @Transactional
    public void updateUser(Long id, Utilisateur details) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        String emailCible = user.getEmail();
        user.setNom(details.getNom());
        user.setEmail(details.getEmail());
        user.setTelephone(details.getTelephone());
        user.setAdresse(details.getAdresse());
        user.setRole(details.getRole());
        auditService.enregistrerLog("MODIFICATION_COMPTE", emailCible, "Modification définitive de l'utilisateur et de ses données");
        utilisateurRepository.save(user);
    }

    public void changerStatut(Long id, String statut) {

        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 1. On garde trace de l'ancien statut pour le log
        String ancienStatut = user.getStatut();

        // 2. Mise à jour du statut
        user.setStatut(statut);
        utilisateurRepository.save(user);

        // 3. Détermination du type d'action pour le badge dans Angular
        String typeAction = statut.equals("BLOQUE") ? "BLOCAGE_UTILISATEUR" : "ACTIVATION_UTILISATEUR";

        // 4. Enregistrement du LOG d'audit
        auditService.enregistrerLog(
                typeAction,
                user.getEmail(),
                "L'administrateur a changé le statut de l'utilisateur. Ancien: " + ancienStatut + " -> Nouveau: " + statut
        );
    }
}