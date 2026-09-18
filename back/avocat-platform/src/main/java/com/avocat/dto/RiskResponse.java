package com.avocat.dto;

import lombok.Data;
import java.util.List;

@Data
public class RiskResponse {
    private String dossierRef;
    private List<String> alertes;
    private String niveauRisque;
    private int score;
}