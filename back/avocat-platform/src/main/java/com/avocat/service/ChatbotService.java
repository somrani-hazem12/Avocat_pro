package com.avocat.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final AuditService auditService;
    private final Counter ocrDocumentsTraitesCounter;   // <-- AJOUT
    private final Timer ocrTraitementLatenceTimer;       // <-- AJOUT

    public String getSummaryFromResource(Resource resource) {
        String url = "http://localhost:8000/summarize";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        auditService.enregistrerLog(
                "IA_RESUME_DOCUMENT",
                resource.getFilename(),
                "Demande de résumé automatique via le chatbot"
        );

        Timer.Sample sample = Timer.start(); // <-- AJOUT : demarre le chrono
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            ocrDocumentsTraitesCounter.increment(); // <-- AJOUT : uniquement si succes
            return response.getBody();
        } catch (Exception e) {
            return "Erreur serveur IA (Python) : " + e.getMessage();
        } finally {
            sample.stop(ocrTraitementLatenceTimer); // <-- AJOUT : temps mesure, succes ou echec
        }
    }
}