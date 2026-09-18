package com.avocat.service;

import com.avocat.dto.RiskResponse;
import com.avocat.entity.*;
import com.avocat.repository.*;
import io.micrometer.core.instrument.Counter; // <-- AJOUT
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiskService {

    private final DocumentRepository documentRepository;
    private final DossierRepository dossierRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSeuilRepository seuilRepository;
    private final Counter alertesRisqueEleveCounter; // <-- AJOUT

    public RiskService(DocumentRepository documentRepository,
                       DossierRepository dossierRepository,
                       NotificationRepository notificationRepository,
                       NotificationSeuilRepository seuilRepository,
                       Counter alertesRisqueEleveCounter) { // <-- AJOUT
        this.documentRepository = documentRepository;
        this.dossierRepository = dossierRepository;
        this.notificationRepository = notificationRepository;
        this.seuilRepository = seuilRepository;
        this.alertesRisqueEleveCounter = alertesRisqueEleveCounter; // <-- AJOUT
    }

    // Récupère les seuils personnalisés ou valeurs par défaut
    private NotificationSeuil getSeuils(Long utilisateurId) {
        return seuilRepository.findByUtilisateurId(utilisateurId)
                .orElseGet(() -> {
                    NotificationSeuil defaut = new NotificationSeuil();
                    return defaut;
                });
    }

    public RiskResponse analyse(Dossier dossier, Utilisateur utilisateur) {
        RiskResponse response = new RiskResponse();
        response.setDossierRef(dossier.getReference());

        NotificationSeuil seuils = getSeuils(utilisateur.getId());

        List<String> alertes = new ArrayList<>();
        int score = 0;

        // Inactivité — seuil personnalisable
        if (dossier.getLastActivity() != null) {
            long jours = ChronoUnit.DAYS.between(
                    dossier.getLastActivity(), LocalDate.now());
            if (jours > seuils.getSeuilInactivite()) {
                alertes.add("Dossier inactif depuis " + jours + " jours (seuil: " + seuils.getSeuilInactivite() + "j)");
                score += 25;
                creerNotification(utilisateur, dossier,
                        "Inactivité détectée",
                        "Le dossier " + dossier.getReference() + " est inactif depuis " + jours + " jours",
                        "INACTIVITE", score < 60 ? "MOYEN" : "ELEVE");
            }
        } else {
            alertes.add("Aucune activité enregistrée");
            score += 20;
        }

        // Deadline — seuil personnalisable
        if (dossier.getDeadline() != null) {
            long joursRestants = ChronoUnit.DAYS.between(
                    LocalDate.now(), dossier.getDeadline());
            if (joursRestants < 0) {
                alertes.add("⚠️ Deadline dépassée de " + Math.abs(joursRestants) + " jours !");
                score += 50;
                creerNotification(utilisateur, dossier,
                        "Deadline dépassée !",
                        "Le dossier " + dossier.getReference() + " a dépassé sa deadline !",
                        "DEADLINE", "ELEVE");
            } else if (joursRestants <= seuils.getSeuilDeadline()) {
                alertes.add("Deadline proche : " + joursRestants + " jours restants (seuil: " + seuils.getSeuilDeadline() + "j)");
                score += 35;
                creerNotification(utilisateur, dossier,
                        "Deadline proche",
                        "Le dossier " + dossier.getReference() + " expire dans " + joursRestants + " jours",
                        "DEADLINE", "MOYEN");
            } else if (joursRestants <= 15) {
                alertes.add("Deadline dans " + joursRestants + " jours");
                score += 10;
            }
        } else {
            alertes.add("Aucune deadline définie");
            score += 15;
        }

        // Documents — seuil personnalisable
        long nbDocuments = documentRepository.countByDossierId(dossier.getId());
        if (nbDocuments == 0) {
            alertes.add("Aucun document dans ce dossier");
            score += 40;
            creerNotification(utilisateur, dossier,
                    "Documents manquants",
                    "Le dossier " + dossier.getReference() + " n'a aucun document",
                    "DOCUMENT", "ELEVE");
        } else if (nbDocuments < seuils.getSeuilDocuments()) {
            alertes.add("Documents insuffisants (" + nbDocuments + "/" + seuils.getSeuilDocuments() + ")");
            score += 30;
            creerNotification(utilisateur, dossier,
                    "Documents insuffisants",
                    "Le dossier " + dossier.getReference() + " a seulement " + nbDocuments + " document(s)",
                    "DOCUMENT", "MOYEN");
        }

        // Ancienneté — seuil personnalisable
        if ("OUVERT".equals(dossier.getStatut())) {
            long joursDepuisCreation = ChronoUnit.DAYS.between(
                    dossier.getDateCreation(), LocalDate.now());
            if (joursDepuisCreation > seuils.getSeuilAnciennete()) {
                alertes.add("Dossier ouvert depuis " + joursDepuisCreation + " jours (seuil: " + seuils.getSeuilAnciennete() + "j)");
                score += 20;
            }
        }

        // Niveau
        String niveau;
        if (score < 30) niveau = "Faible";
        else if (score < 60) niveau = "Moyen";
        else niveau = "Élevé";

        response.setAlertes(alertes);
        response.setScore(Math.min(score, 100));
        response.setNiveauRisque(niveau);
        return response;
    }

    private void creerNotification(Utilisateur utilisateur, Dossier dossier,
                                   String titre, String message,
                                   String type, String niveau) {
        // Éviter les doublons — vérifier si notification récente existe
        List<Notification> existantes = notificationRepository
                .findByUtilisateurIdOrderByDateCreationDesc(utilisateur.getId());

        boolean dejaExiste = existantes.stream().anyMatch(n ->
                n.getDossierRef() != null &&
                        n.getDossierRef().equals(dossier.getReference()) &&
                        n.getType().equals(type) &&
                        !n.isLue() &&
                        ChronoUnit.HOURS.between(n.getDateCreation(), LocalDateTime.now()) < 24
        );

        if (!dejaExiste) {
            Notification notif = new Notification();
            notif.setTitre(titre);
            notif.setMessage(message);
            notif.setType(type);
            notif.setNiveau(niveau);
            notif.setUtilisateur(utilisateur);
            notif.setDossierRef(dossier.getReference());
            notificationRepository.save(notif);

            // <-- AJOUT : ne compter que les vraies nouvelles alertes ELEVE
            // (pas les doublons filtres par dejaExiste ci-dessus)
            if ("ELEVE".equals(niveau)) {
                alertesRisqueEleveCounter.increment();
            }
        }
    }

    public List<RiskResponse> analyserTous(Long utilisateurId, Utilisateur utilisateur) {
        List<Dossier> dossiers = dossierRepository.findByUtilisateurId(utilisateurId);
        List<RiskResponse> resultats = new ArrayList<>();
        for (Dossier d : dossiers) {
            resultats.add(analyse(d, utilisateur));
        }
        return resultats;
    }
}