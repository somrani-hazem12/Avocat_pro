package com.avocat.controller;

import com.avocat.dto.RiskResponse;
import com.avocat.entity.Dossier;
import com.avocat.entity.Utilisateur;
import com.avocat.repository.DossierRepository;
import com.avocat.repository.UtilisateurRepository;
import com.avocat.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;
    private final DossierRepository dossierRepository;
    private final UtilisateurRepository utilisateurRepository;

    // Analyser un dossier spécifique
    @GetMapping("/dossier/{id}")
    public ResponseEntity<RiskResponse> analyserDossier(
            @PathVariable Long id,
            Authentication auth) {
        Utilisateur user = utilisateurRepository
                .findByEmail(auth.getName()).orElseThrow();
        return dossierRepository.findById(id)
                .map(dossier -> ResponseEntity.ok(
                        riskService.analyse(dossier, user)))  // ✅ 2 arguments
                .orElse(ResponseEntity.notFound().build());
    }

    // Analyser tous les dossiers de l'utilisateur connecté
    @GetMapping("/mes-dossiers")
    public ResponseEntity<List<RiskResponse>> analyserMesDossiers(
            Authentication auth) {
        Utilisateur user = utilisateurRepository
                .findByEmail(auth.getName()).orElseThrow();
        List<RiskResponse> resultats = riskService.analyserTous(
                user.getId(), user);  // ✅ 2 arguments
        return ResponseEntity.ok(resultats);
    }

    // Dashboard risques — stats globales
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboardRisques(Authentication auth) {
        Utilisateur user = utilisateurRepository
                .findByEmail(auth.getName()).orElseThrow();
        List<RiskResponse> tous = riskService.analyserTous(
                user.getId(), user);  // ✅ 2 arguments

        long faible = tous.stream()
                .filter(r -> "Faible".equals(r.getNiveauRisque())).count();
        long moyen  = tous.stream()
                .filter(r -> "Moyen".equals(r.getNiveauRisque())).count();
        long eleve  = tous.stream()
                .filter(r -> "Élevé".equals(r.getNiveauRisque())).count();

        return ResponseEntity.ok(Map.of(
                "total",   tous.size(),
                "faible",  faible,
                "moyen",   moyen,
                "eleve",   eleve,
                "details", tous
        ));
    }
}