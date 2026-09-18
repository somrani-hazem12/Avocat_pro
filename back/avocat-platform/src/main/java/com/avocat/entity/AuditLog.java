package com.avocat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;      // Type d'action (LOGIN, GENERATION_DOC, etc.)
    private String utilisateur; // Email de la personne connectée
    private String cible;       // Sur quoi porte l'action (ex: Nom du client ou ID dossier)

    @Column(columnDefinition = "TEXT")
    private String details;     // Description détaillée

    private String adresseIp;
    private LocalDateTime dateAction;
}