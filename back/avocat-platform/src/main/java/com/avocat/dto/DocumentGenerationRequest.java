package com.avocat.dto;

import lombok.Data;
import java.util.Map;
@Data
public class DocumentGenerationRequest {
    private String templateId; // ex: "mise-en-demeure"
    private Map<String, Object> data; // Contient toutes les variables du formulaire
}
