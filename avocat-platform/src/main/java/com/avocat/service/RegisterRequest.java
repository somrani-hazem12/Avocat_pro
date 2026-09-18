package com.avocat.service;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String motDePasse;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String specialite;
    private String role;
}