package com.avocat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String message;
    private String type; // RISQUE, DEADLINE, INACTIVITE, DOCUMENT
    private String niveau; // FAIBLE, MOYEN, ELEVE
    private boolean lue = false;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    private String dossierRef;
}