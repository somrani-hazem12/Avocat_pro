package com.avocat.controller;

import com.avocat.entity.Client;
import com.avocat.entity.Dossier;
import com.avocat.entity.Utilisateur;
import com.avocat.repository.ClientRepository;
import com.avocat.repository.DossierRepository;
import com.avocat.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DossierController {

    private final DossierRepository dossierRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;

    @GetMapping
    public List<Dossier> getAll(Authentication auth, @RequestParam(required = false) Long clientId) {
        Utilisateur userConnecte = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        if (clientId != null && "AVOCAT".equals(userConnecte.getRole())) {
            return dossierRepository.findByUtilisateurIdAndClient_Utilisateur_Id(userConnecte.getId(), clientId);
        }

        if ("ADMINISTRATEUR".equals(userConnecte.getRole())) {
            return dossierRepository.findAll();
        }

        if ("CLIENT".equals(userConnecte.getRole())) {
            return dossierRepository.findByClient_Utilisateur_Id(userConnecte.getId());
        }

        return dossierRepository.findByUtilisateurId(userConnecte.getId());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Dossier dossier, Authentication auth) {
        try {
            // 1. Récupérer l'avocat connecté
            Utilisateur avocat = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

            // 2. Vérifier client
            if (dossier.getClient() == null || dossier.getClient().getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Veuillez sélectionner un client"));
            }
            Long idUtilisateurClient = dossier.getClient().getId();

            // 3. Trouver sa fiche réelle dans la table clients
            Client profilClient = clientRepository.findByUtilisateurId(idUtilisateurClient)
                    .orElseThrow(() -> new RuntimeException("Profil client manquant pour cet utilisateur"));

            // 4. Assigner les données
            dossier.setUtilisateur(avocat);
            dossier.setClient(profilClient);
            dossier.setDateCreation(LocalDate.now());
            dossier.setLastActivity(LocalDate.now());                  // ✅ activité = aujourd'hui
            dossier.setDeadline(LocalDate.now().plusDays(20));         // ✅ deadline = aujourd'hui + 20 jours
            if (dossier.getStatut() == null) dossier.setStatut("OUVERT");

            return ResponseEntity.ok(dossierRepository.save(dossier));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Dossier dossier,
                                    Authentication auth) {
        try {
            return dossierRepository.findById(id).map(existing -> {
                existing.setReference(dossier.getReference());
                existing.setType(dossier.getType());
                existing.setStatut(dossier.getStatut());
                existing.setLastActivity(LocalDate.now());             // ✅ mise à jour automatique
                return ResponseEntity.ok(dossierRepository.save(existing));
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dossierRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dossier> getById(@PathVariable Long id) {
        return dossierRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}