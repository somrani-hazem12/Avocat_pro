package com.avocat.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "notification_seuils")
@Data
public class NotificationSeuil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    // Seuil inactivité en jours (défaut 15)
    private int seuilInactivite = 15;

    // Seuil deadline en jours (défaut 5)
    private int seuilDeadline = 5;

    // Seuil documents minimum (défaut 2)
    private int seuilDocuments = 2;

    // Seuil ancienneté dossier en jours (défaut 90)
    private int seuilAnciennete = 90;
}