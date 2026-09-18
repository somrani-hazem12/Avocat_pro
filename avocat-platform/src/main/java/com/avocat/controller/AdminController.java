package com.avocat.controller;

import com.avocat.entity.Utilisateur;
import com.avocat.repository.UtilisateurRepository;
import com.avocat.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/en-attente")
    public List<Utilisateur> getInscriptionsEnAttente() {
        return adminService.getInscriptionsEnAttente();
    }

    @PutMapping("/accepter/{id}")
    public ResponseEntity<?> accepterUtilisateur(@PathVariable Long id) {
        try {
            adminService.accepterUtilisateur(id);
            return ResponseEntity.ok().body(Map.of("message", "Utilisateur validé"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/refuser/{id}")
    public ResponseEntity<?> refuserUtilisateur(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            adminService.refuserUtilisateur(id, body.get("motif"));
            return ResponseEntity.ok().body(Map.of("message", "Utilisateur refusé"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/utilisateurs")
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @PutMapping("/utilisateurs/{id}/statut")
    public ResponseEntity<?> changerStatut(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            System.out.println("=== CHANGEMENT STATUT ===");
            System.out.println("ID: " + id);
            System.out.println("Nouveau statut: " + body.get("statut"));

            Utilisateur user = utilisateurRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Utilisateur non trouvé"));
            }
            user.setStatut(body.get("statut"));
            utilisateurRepository.save(user);

            System.out.println("Statut modifié avec succès");
            return ResponseEntity.ok().body(Map.of("message", "Statut modifié avec succès"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<?> supprimerUtilisateur(@PathVariable Long id) {
        try {
            if (utilisateurRepository.existsById(id)) {
                utilisateurRepository.deleteById(id);
                return ResponseEntity.ok().body(Map.of("message", "Utilisateur supprimé avec succès"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Utilisateur non trouvé"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }
}