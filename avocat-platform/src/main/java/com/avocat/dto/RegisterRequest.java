package com.avocat.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nom;
    private String email;
    private String motDePasse;
    private String role; // AVOCAT, ASSISTANT, CLIENT
    private String telephone;
    private String adresse;
    private String specialite;
}