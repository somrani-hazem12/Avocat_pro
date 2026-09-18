package com.avocat.controller;

import com.avocat.entity.Notification;
import com.avocat.entity.NotificationSeuil;
import com.avocat.entity.Utilisateur;
import com.avocat.repository.NotificationRepository;
import com.avocat.repository.NotificationSeuilRepository;
import com.avocat.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationSeuilRepository seuilRepository;
    private final UtilisateurRepository utilisateurRepository;

    // Toutes les notifications
    @GetMapping
    public List<Notification> getMesNotifications(Authentication auth) {
        Utilisateur user = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        return notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(user.getId());
    }

    // Notifications non lues
    @GetMapping("/non-lues")
    public List<Notification> getNonLues(Authentication auth) {
        Utilisateur user = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        return notificationRepository.findByUtilisateurIdAndLueFalse(user.getId());
    }

    // Compteur non lues
    @GetMapping("/count")
    public Map<String, Long> getCount(Authentication auth) {
        Utilisateur user = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        long count = notificationRepository.countByUtilisateurIdAndLueFalse(user.getId());
        return Map.of("count", count);
    }

    // Marquer comme lue
    @PutMapping("/{id}/lire")
    public ResponseEntity<?> marquerLue(@PathVariable Long id) {
        return notificationRepository.findById(id).map(notif -> {
            notif.setLue(true);
            notificationRepository.save(notif);
            return ResponseEntity.ok(Map.of("message", "Notification lue"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Tout marquer comme lu
    @PutMapping("/lire-tout")
    public ResponseEntity<?> marquerToutLu(Authentication auth) {
        Utilisateur user = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        List<Notification> nonLues = notificationRepository.findByUtilisateurIdAndLueFalse(user.getId());
        nonLues.forEach(n -> n.setLue(true));
        notificationRepository.saveAll(nonLues);
        return ResponseEntity.ok(Map.of("message", "Tout marqué comme lu"));
    }

    // Supprimer une notification
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        notificationRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ===== SEUILS PERSONNALISABLES =====

    // Récupérer les seuils
    @GetMapping("/seuils")
    public ResponseEntity<?> getSeuils(Authentication auth) {
        Utilisateur user = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        NotificationSeuil seuils = seuilRepository.findByUtilisateurId(user.getId())
                .orElseGet(() -> {
                    NotificationSeuil defaut = new NotificationSeuil();
                    defaut.setUtilisateur(user);
                    return seuilRepository.save(defaut);
                });
        return ResponseEntity.ok(seuils);
    }

    // Sauvegarder les seuils
    @PutMapping("/seuils")
    public ResponseEntity<?> saveSeuils(Authentication auth,
                                        @RequestBody NotificationSeuil nouveauxSeuils) {
        Utilisateur user = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        NotificationSeuil seuils = seuilRepository.findByUtilisateurId(user.getId())
                .orElseGet(() -> {
                    NotificationSeuil n = new NotificationSeuil();
                    n.setUtilisateur(user);
                    return n;
                });
        seuils.setSeuilInactivite(nouveauxSeuils.getSeuilInactivite());
        seuils.setSeuilDeadline(nouveauxSeuils.getSeuilDeadline());
        seuils.setSeuilDocuments(nouveauxSeuils.getSeuilDocuments());
        seuils.setSeuilAnciennete(nouveauxSeuils.getSeuilAnciennete());
        return ResponseEntity.ok(seuilRepository.save(seuils));
    }
}