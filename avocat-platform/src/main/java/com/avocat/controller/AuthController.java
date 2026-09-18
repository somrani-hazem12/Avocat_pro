package com.avocat.controller;

import com.avocat.dto.LoginRequest;
import com.avocat.dto.LoginResponse;
import com.avocat.entity.Utilisateur;
import com.avocat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200") // Autorise Angular
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint pour la connexion
     * Utilise LoginRequest pour mapper le JSON { "email": "...", "motDePasse": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Appelle le service et récupère l'objet LoginResponse (token, role, nom, id)
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Renvoie une erreur 401 (Non autorisé) avec le message d'erreur
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint pour l'inscription
     * Ici on utilise l'entité Utilisateur directement ou un RegisterRequest
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Utilisateur utilisateur) {
        try {
            String message = authService.register(utilisateur);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            // Renvoie une erreur 400 (Bad Request)
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}