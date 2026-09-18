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

        // 1. CAS : L'avocat filtre un client précis (depuis "Mes Clients")
        // ON FILTRE PAR : ID AVOCAT CONNECTÉ + ID CLIENT PASSÉ
        if (clientId != null && "AVOCAT".equals(userConnecte.getRole())) {
            return dossierRepository.findByUtilisateurIdAndClient_Utilisateur_Id(userConnecte.getId(), clientId);
        }

        // 2. CAS : Administrateur (voit tout)
        if ("ADMINISTRATEUR".equals(userConnecte.getRole())) {
            return dossierRepository.findAll();
        }

        // 3. CAS : Client (voit ses dossiers personnels)
        if ("CLIENT".equals(userConnecte.getRole())) {
            return dossierRepository.findByClient_Utilisateur_Id(userConnecte.getId());
        }

        // 4. CAS : Avocat (sans filtre) voit TOUS les dossiers dont il est responsable
        return dossierRepository.findByUtilisateurId(userConnecte.getId());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Dossier dossier, Authentication auth) {
        try {
            // 1. Récupérer l'avocat connecté
            Utilisateur avocat = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

            // 2. Récupérer l'ID utilisateur du client (envoyé par Angular)
            if (dossier.getClient() == null || dossier.getClient().getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Veuillez sélectionner un client"));
            }
            Long idUtilisateurClient = dossier.getClient().getId();

            // 3. FIX : Trouver sa fiche réelle dans la table 'clients'
            Client profilClient = clientRepository.findByUtilisateurId(idUtilisateurClient)
                    .orElseThrow(() -> new RuntimeException("Profil client manquant pour cet utilisateur"));

            // 4. Assigner les données
            dossier.setUtilisateur(avocat);
            dossier.setClient(profilClient); // On utilise l'objet Client, pas Utilisateur
            dossier.setDateCreation(LocalDate.now());
            if (dossier.getStatut() == null) dossier.setStatut("OUVERT");

            return ResponseEntity.ok(dossierRepository.save(dossier));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}