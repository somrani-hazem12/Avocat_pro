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
        // Cette ligne appelle maintenant la méthode existante dans AdminService
        return adminService.getInscriptionsEnAttente();
    }

    @PutMapping("/accepter/{id}")
    public ResponseEntity<?> accepterUtilisateur(@PathVariable Long id) {
        try {
            adminService.accepterUtilisateur(id);
            return ResponseEntity.ok().body("{\"message\": \"Utilisateur validé\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/refuser/{id}")
    public ResponseEntity<?> refuserUtilisateur(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            adminService.refuserUtilisateur(id, body.get("motif"));
            return ResponseEntity.ok().body("{\"message\": \"Utilisateur refusé\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
    @GetMapping("/utilisateurs")
    public List<Utilisateur> getAll() {
        // Renvoie tous les utilisateurs pour que l'admin puisse les gérer
        return utilisateurRepository.findAll();
    }

    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok().body(Map.of("message", "Utilisateur et ses données supprimés"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur lors de la suppression : " + e.getMessage()));
        }
    }

    @PutMapping("/utilisateurs/{id}/statut")
    public ResponseEntity<?> changerStatut(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String nouveauStatut = body.get("statut");
            adminService.changerStatut(id, nouveauStatut);
            return ResponseEntity.ok().body(Map.of("message", "Le statut de l'utilisateur est maintenant : " + nouveauStatut));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur : " + e.getMessage()));
        }
    }
    @PutMapping("/modifier/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Utilisateur details) {
        try {
            adminService.updateUser(id, details);
            return ResponseEntity.ok().body(Map.of("message", "Utilisateur mis à jour"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}