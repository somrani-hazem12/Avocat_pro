package com.avocat.controller;

import com.avocat.entity.Client;
import com.avocat.entity.RendezVous;
import com.avocat.entity.Utilisateur;
import com.avocat.repository.ClientRepository;
import com.avocat.repository.RendezVousRepository;
import com.avocat.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rendez-vous")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RendezVousController {

    private final RendezVousRepository rendezvousRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;

    @GetMapping
    public List<RendezVous> getAll(Authentication auth) {
        Utilisateur user = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        if ("CLIENT".equals(user.getRole())) {
            return rendezvousRepository.findByClientUtilisateurId(user.getId());
        }
        return rendezvousRepository.findByUtilisateurId(user.getId());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody RendezVous rdv, Authentication auth) {
        Utilisateur currentUser = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        if ("CLIENT".equals(currentUser.getRole())) {
            Client clientEntity = clientRepository.findByUtilisateurId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Profil client non trouvé"));
            rdv.setClient(clientEntity);

            Utilisateur avocat = utilisateurRepository.findById(rdv.getUtilisateur().getId()).orElseThrow();
            rdv.setUtilisateur(avocat);
            rdv.setStatut("DEMANDE");
        }
        return ResponseEntity.ok(rendezvousRepository.save(rdv));
    }

    @PutMapping("/{id}/confirmer")
    public ResponseEntity<?> confirmer(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return rendezvousRepository.findById(id).map(rdv -> {
            rdv.setDateHeure(LocalDateTime.parse(body.get("dateHeure")));
            rdv.setStatut("CONFIRME");
            rendezvousRepository.save(rdv);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}