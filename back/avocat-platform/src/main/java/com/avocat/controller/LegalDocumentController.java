package com.avocat.controller;

import com.avocat.dto.DocumentGenerationRequest;
import com.avocat.service.DocumentGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/legal-documents")
@CrossOrigin(origins = "*")
public class LegalDocumentController {

    private final DocumentGeneratorService documentGeneratorService;

    // Injection du service
    public LegalDocumentController(DocumentGeneratorService documentGeneratorService) {
        this.documentGeneratorService = documentGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@RequestBody DocumentGenerationRequest request) {
        try {
            // Utilisation exacte de ton service et de ton DTO
            byte[] pdfBytes = documentGeneratorService.generatePdf(
                    request.getTemplateId(),
                    request.getData()
            );

            // Configuration des headers pour l'envoi du fichier PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", request.getTemplateId() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}