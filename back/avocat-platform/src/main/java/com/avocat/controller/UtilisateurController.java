package com.avocat.controller;

import com.avocat.entity.Client;
import com.avocat.entity.SuiviAvocat;
import com.avocat.entity.Utilisateur;
import com.avocat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/utilisateur")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final SuiviAvocatRepository suiviAvocatRepository;
    private final DossierRepository dossierRepository;
    private final RendezVousRepository rendezvousRepository;
    private final ClientRepository clientRepository; // AJOUTÉ POUR LE PROFIL CLIENT
    private final PasswordEncoder passwordEncoder;

    /**
     * Récupère le profil avec la liste des avocats partenaires
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        Utilisateur user = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        List<Utilisateur> mesAvocats = suiviAvocatRepository.findByClientId(user.getId())
                .stream().map(SuiviAvocat::getAvocat).collect(Collectors.toList());

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("nom", user.getNom());
        profile.put("role", user.getRole());
        profile.put("mesAvocats", mesAvocats);
        return ResponseEntity.ok(profile);
    }

    /**
     * Statistiques réelles pour l'avocat
     */
    @GetMapping("/stats-cabinet")
    public ResponseEntity<Map<String, Long>> getStatsCabinet(Authentication auth) {
        Utilisateur avocat = utilisateurRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Avocat non trouvé"));

        Map<String, Long> stats = new HashMap<>();
        stats.put("clients", suiviAvocatRepository.countByAvocatId(avocat.getId()));
        stats.put("dossiers", dossierRepository.countByUtilisateurId(avocat.getId()));
        stats.put("rdvAttente", rendezvousRepository.countByUtilisateurIdAndStatut(avocat.getId(), "DEMANDE"));

        return ResponseEntity.ok(stats);
    }

    /**
     * Action de choisir un avocat (pour le client)
     */
    @PostMapping("/choisir-avocat/{avocatId}")
    @Transactional
    public ResponseEntity<?> choisirAvocat(@PathVariable Long avocatId, Authentication auth) {
        Utilisateur client = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        Utilisateur avocat = utilisateurRepository.findById(avocatId).orElseThrow();

        if (suiviAvocatRepository.findByClientIdAndAvocatId(client.getId(), avocat.getId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vous suivez déjà cet avocat"));
        }

        SuiviAvocat suivi = new SuiviAvocat();
        suivi.setClient(client);
        suivi.setAvocat(avocat);
        suiviAvocatRepository.save(suivi);

        return ResponseEntity.ok(Map.of("message", "Lien de partenariat créé"));
    }

    /**
     * Rompre le lien entre un avocat et un client
     */
    @DeleteMapping("/retirer-avocat/{id}")
    @Transactional
    public ResponseEntity<?> retirerLien(@PathVariable Long id, Authentication auth) {
        Utilisateur current = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        List<SuiviAvocat> liensEnDouble;

        if ("AVOCAT".equals(current.getRole())) {
            // L'avocat retire un client
            liensEnDouble = suiviAvocatRepository.findAllByClientIdAndAvocatId(id, current.getId());
        } else {
            // Le client retire un avocat
            liensEnDouble = suiviAvocatRepository.findAllByClientIdAndAvocatId(current.getId(), id);
        }

        // Si on trouve des liens (un seul ou plusieurs doublons), on les supprime tous
        if (!liensEnDouble.isEmpty()) {
            suiviAvocatRepository.deleteAll(liensEnDouble);
        }

        return ResponseEntity.ok(Map.of("message", "Lien rompu"));
    }



    /**
     * Modifier les informations d'un client (évite les doublons)
     */
    @PutMapping("/modifier-client/{id}")
    @Transactional
    public ResponseEntity<?> modifierClient(@PathVariable Long id, @RequestBody Utilisateur details) {
        return utilisateurRepository.findById(id).map(user -> {
            user.setNom(details.getNom());
            user.setEmail(details.getEmail());
            user.setTelephone(details.getTelephone());
            user.setAdresse(details.getAdresse());
            utilisateurRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Mis à jour"));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Liste des clients de l'avocat connecté
     */
    @GetMapping("/mes-clients")
    public List<Utilisateur> getMesClients(Authentication auth) {
        Utilisateur avocat = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        return suiviAvocatRepository.findByAvocatId(avocat.getId())
                .stream().map(SuiviAvocat::getClient).collect(Collectors.toList());
    }

    /**
     * AJOUT MANUEL : Crée l'Utilisateur + le Profil Client + le Lien de suivi
     */
    @PostMapping("/ajouter-client-manuel")
    @Transactional
    public ResponseEntity<?> ajouterClientManuel(@RequestBody Utilisateur nouveauClient, Authentication auth) {
        Utilisateur avocat = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        // 1. Créer le compte Utilisateur
        nouveauClient.setMotDePasse(passwordEncoder.encode("client123"));
        nouveauClient.setRole("CLIENT");
        nouveauClient.setStatut("ACTIF");
        Utilisateur savedUser = utilisateurRepository.save(nouveauClient);

        // 2. Créer le profil Client (Indispensable pour les Rendez-vous)
        Client clientProfile = new Client();
        clientProfile.setNom(savedUser.getNom());
        clientProfile.setEmail(savedUser.getEmail());
        clientProfile.setTelephone(savedUser.getTelephone());
        clientProfile.setAdresse(savedUser.getAdresse());
        clientProfile.setUtilisateur(savedUser);
        clientRepository.save(clientProfile);

        // 3. Créer le lien de suivi entre l'avocat et ce nouveau client
        SuiviAvocat suivi = new SuiviAvocat();
        suivi.setClient(savedUser);
        suivi.setAvocat(avocat);
        suiviAvocatRepository.save(suivi);

        return ResponseEntity.ok(Map.of("message", "Client ajouté et profil créé"));
    }

    /**
     * Liste complète des avocats pour la recherche client
     */
    @GetMapping("/avocats-disponibles")
    public List<Utilisateur> getAvocatsDisponibles() {
        return utilisateurRepository.findByRole("AVOCAT");
    }
}