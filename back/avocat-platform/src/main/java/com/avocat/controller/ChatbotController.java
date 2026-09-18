package com.avocat.controller;

import com.avocat.entity.Document;
import com.avocat.repository.DocumentRepository;
import com.avocat.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final DocumentRepository documentRepository;

    @PostMapping("/summarize/{id}")
    public ResponseEntity<String> summarize(@PathVariable Long id) {
        try {
            // 1. Récupérer le document depuis la base de données (données binaires)
            Document doc = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Document non trouvé en base"));

            // 2. Transformer le byte[] (contenu) en ressource pour l'envoyer à Python
            // On ajoute une petite classe anonyme pour donner un nom au fichier
            ByteArrayResource resource = new ByteArrayResource(doc.getContenu()) {
                @Override
                public String getFilename() {
                    return doc.getNom();
                }
            };

            // 3. Envoyer à l'IA
            String summary = chatbotService.getSummaryFromResource(resource);
            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors du résumé : " + e.getMessage());
        }
    }
}