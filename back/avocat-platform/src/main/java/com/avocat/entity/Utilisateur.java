package com.avocat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Data
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore // Sécurité : Ne jamais renvoyer le mot de passe dans les JSON
    @Column(nullable = false)
    private String motDePasse;

    @Column(nullable = false)
    private String role; // AVOCAT, ASSISTANT, CLIENT, ADMINISTRATEUR

    @Column(nullable = false)
    private String statut = "EN_ATTENTE"; // EN_ATTENTE, ACTIF, BLOQUE

    private String telephone;
    private String adresse;
    private String specialite;

    @Column(nullable = false)
    private LocalDateTime dateInscription = LocalDateTime.now();

    private String motifRefus;

    // =============================================
    // AJOUT SÉCURITÉ : Protection contre Force Brute
    // =============================================
    @Column(nullable = false)
    private int echecsConnexion = 0; // Nombre de tentatives ratées

    // Optionnel : On définit la limite ici pour la réutiliser dans le service
    public static final int MAX_TENTATIVES = 5;
}