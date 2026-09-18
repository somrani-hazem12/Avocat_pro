package com.avocat.service;

import com.avocat.dto.LoginRequest;
import com.avocat.dto.LoginResponse; // Utilisation de ton DTO
import com.avocat.entity.Utilisateur;
import com.avocat.repository.UtilisateurRepository;
import com.avocat.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé en base"));

        System.out.println("DEBUG LOGIN: Email=" + user.getEmail());
        System.out.println("DEBUG LOGIN: Statut=" + user.getStatut());
        System.out.println("DEBUG LOGIN: Pass_Saisi=" + request.getMotDePasse());
        System.out.println("DEBUG LOGIN: Pass_Base=" + user.getMotDePasse());

        if (!request.getMotDePasse().equals(user.getMotDePasse())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        if (!"ACTIF".equals(user.getStatut())) {
            throw new RuntimeException("Compte non actif (Statut: " + user.getStatut() + ")");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getRole(), user.getNom(), user.getId());
    }

    public String register(Utilisateur user) {
        if (utilisateurRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // On force le statut à ACTIF pour que tu puisses te connecter immédiatement pour tes tests
        user.setStatut("ACTIF");

        utilisateurRepository.save(user);
        return "Inscription réussie !";
    }
}