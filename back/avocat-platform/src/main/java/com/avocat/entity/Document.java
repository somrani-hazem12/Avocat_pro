package com.avocat.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String type;

    // --- CORRECTION ICI ---
    // On retire @Lob pour éviter l'erreur BigInt/OID sur PostgreSQL
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "contenu")
    private byte[] contenu;

    private LocalDateTime dateUpload = LocalDateTime.now();
    private String uploaderNom;

    @ManyToOne
    @JoinColumn(name = "dossier_id")
    private Dossier dossier;
}