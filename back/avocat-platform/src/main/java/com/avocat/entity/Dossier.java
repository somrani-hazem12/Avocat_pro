package com.avocat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "dossiers")
@Data
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    private String type;
    private String statut = "OUVERT";
    private LocalDate dateCreation = LocalDate.now();
    // NOUVEAU
    @Column(name = "last_activity")
    private LocalDate lastActivity;

    // NOUVEAU
    @Column(name = "deadline")
    private LocalDate deadline;
    // L'avocat (Utilisateur)
    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    // LE CLIENT (Doit pointer vers l'entité Client, table 'clients')
    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;
}