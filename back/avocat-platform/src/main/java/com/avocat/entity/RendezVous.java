package com.avocat.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // IMPORT À AJOUTER
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "rendez_vous")
@Data
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateHeure; // Fixée par l'avocat plus tard
    private String motif; // Rempli par le client

    @Column(nullable = false)
    private String statut = "DEMANDE";

    @ManyToOne
    @JoinColumn(name = "client_id")
    // FIX : Empêche Spring de tourner en boucle entre le RDV et le Client
    @JsonIgnoreProperties({"rendezVous", "dossiers", "factures", "utilisateur"})
    private Client client;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    // FIX : Empêche Spring de renvoyer le mot de passe de l'avocat pour plus de sécurité
    @JsonIgnoreProperties({"motDePasse", "dateInscription", "statut", "adresse"})
    private Utilisateur utilisateur;
}