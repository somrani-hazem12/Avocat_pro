package com.avocat.entity;

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

    private String nom;
    private String prenom;
    @Column(unique = true)
    private String email;
    private String motDePasse;
    private String role;
    private String statut = "EN_ATTENTE";
    private String telephone;
    private String adresse;
    private String specialite;
    private LocalDateTime dateInscription = LocalDateTime.now();

    // Assure-toi que ce champ est EXACTEMENT écrit comme ça
    private String motifRefus;

    private Long avocatId;
}