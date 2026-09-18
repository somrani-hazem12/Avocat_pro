package com.avocat.controller;

import com.avocat.entity.Client;
import com.avocat.entity.Facture;
import com.avocat.entity.Utilisateur;
import com.avocat.repository.ClientRepository;
import com.avocat.repository.FactureRepository;
import com.avocat.repository.UtilisateurRepository;
import com.avocat.service.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/factures")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class FactureController {

    private final FactureRepository factureRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final FactureService factureService;

    @GetMapping
    public List<Facture> getAll(Authentication auth) {
        Utilisateur user = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        if ("ADMINISTRATEUR".equals(user.getRole())) {
            return factureRepository.findAll();
        }

        if ("CLIENT".equals(user.getRole())) {
            return clientRepository.findByUtilisateurId(user.getId())
                    .map(client -> factureRepository.findByClientId(client.getId()))
                    .orElse(List.of());
        }

        return factureRepository.findByUtilisateurId(user.getId());
    }

    /**
     * FIX : Création de facture avec conversion de l'ID Utilisateur en Profil Client
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Facture facture, Authentication auth) {
        try {
            // 1. Récupérer l'avocat connecté
            Utilisateur creator = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

            // 2. Vérifier si le client est envoyé
            if (facture.getClient() == null || facture.getClient().getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Veuillez sélectionner un client"));
            }

            // 3. FIX : Convertir l'ID Utilisateur reçu en Profil Client (table clients)
            Long idUtilisateurClient = facture.getClient().getId();
            Client profilClient = clientRepository.findByUtilisateurId(idUtilisateurClient)
                    .orElseThrow(() -> new RuntimeException("Le client sélectionné n'a pas de profil actif."));

            // 4. Remplissage des données
            facture.setUtilisateur(creator);
            facture.setClient(profilClient); // On met le vrai profil
            facture.setDate(LocalDate.now());

            // Calcul du Total TTC
            if (facture.getMontantHT() != null && facture.getTva() != null) {
                double total = facture.getMontantHT() * (1 + (facture.getTva() / 100));
                facture.setMontantTotal(total);
            }

            if (facture.getStatut() == null) facture.setStatut("EN_ATTENTE");

            return ResponseEntity.ok(factureRepository.save(facture));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur : " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/payer")
    public ResponseEntity<Facture> marquerCommePayee(@PathVariable Long id) {
        return factureRepository.findById(id).map(f -> {
            f.setStatut("PAYE");
            return ResponseEntity.ok(factureRepository.save(f));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] contents = factureService.genererPdfFacture(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Facture_" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(contents);
    }
}