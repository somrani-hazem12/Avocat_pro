package com.avocat.controller;

import com.avocat.entity.Document;
import com.avocat.entity.Dossier;
import com.avocat.repository.DocumentRepository;
import com.avocat.repository.DossierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
// PAS DE @CrossOrigin ICI, la config globale suffit
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final DossierRepository dossierRepository;

    @PostMapping("/upload/{dossierId}")
    public ResponseEntity<?> upload(@PathVariable Long dossierId,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam("uploader") String uploader) throws IOException {

        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));

        Document doc = new Document();
        doc.setNom(file.getOriginalFilename());
        doc.setType(file.getContentType());
        doc.setContenu(file.getBytes());
        doc.setUploaderNom(uploader);
        doc.setDossier(dossier);

        documentRepository.save(doc);
        return ResponseEntity.ok().body("{\"message\": \"Fichier ajouté avec succès\"}");
    }

    @GetMapping("/dossier/{dossierId}")
    public List<Document> getDocsByDossier(@PathVariable Long dossierId) {
        return documentRepository.findByDossierId(dossierId).stream().map(d -> {
            d.setContenu(null); // On ne renvoie pas le binaire pour la liste
            return d;
        }).collect(Collectors.toList());
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        Document doc = documentRepository.findById(id).orElseThrow();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getNom() + "\"")
                .body(doc.getContenu());
    }
}