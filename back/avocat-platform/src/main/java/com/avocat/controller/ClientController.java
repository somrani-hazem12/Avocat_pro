package com.avocat.controller;

import com.avocat.entity.SuiviAvocat;
import com.avocat.entity.Utilisateur;
import com.avocat.repository.SuiviAvocatRepository;
import com.avocat.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ClientController {

    private final SuiviAvocatRepository suiviAvocatRepository;
    private final UtilisateurRepository utilisateurRepository;

    // 1. Récupérer la liste des avocats qui suivent le client connecté
    @GetMapping("/mes-avocats/{clientId}")
    public List<Utilisateur> getMesAvocats(@PathVariable Long clientId) {
        return suiviAvocatRepository.findByClientId(clientId).stream()
                .map(SuiviAvocat::getAvocat)
                .collect(Collectors.toList());
    }

    // 2. Ajouter un nouvel avocat à sa liste de suivi
    @PostMapping("/choisir-avocat")
    public ResponseEntity<?> choisirAvocat(@RequestParam Long clientId, @RequestParam Long avocatId) {
        // Vérifier si le lien existe déjà
        if (suiviAvocatRepository.findByClientIdAndAvocatId(clientId, avocatId).isPresent()) {
            return ResponseEntity.badRequest().body("{\"message\": \"Vous suivez déjà cet avocat\"}");
        }

        Utilisateur client = utilisateurRepository.findById(clientId).orElseThrow();
        Utilisateur avocat = utilisateurRepository.findById(avocatId).orElseThrow();

        SuiviAvocat suivi = new SuiviAvocat();
        suivi.setClient(client);
        suivi.setAvocat(avocat);
        suiviAvocatRepository.save(suivi);

        return ResponseEntity.ok().body("{\"message\": \"Suivi activé\"}");
    }

    // 3. Liste de TOUS les avocats (pour le dashboard)
    @GetMapping("/tous-les-avocats")
    public List<Utilisateur> getAllAvocats() {
        return utilisateurRepository.findByRole("AVOCAT");
    }
    
}