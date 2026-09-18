package com.avocat.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // IMPORT À AJOUTER
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "suivi_avocats")
@Data
public class SuiviAvocat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"mesAvocats", "motDePasse"}) // Empêche la boucle vers le client
    private Utilisateur client;

    @ManyToOne
    @JoinColumn(name = "avocat_id")
    @JsonIgnoreProperties({"mesAvocats", "motDePasse"}) // Empêche la boucle vers l'avocat
    private Utilisateur avocat;

    private LocalDateTime dateDebut = LocalDateTime.now();
}