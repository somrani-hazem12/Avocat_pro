package com.avocat.controller;

import com.avocat.dto.LoginRequest;
import com.avocat.dto.LoginResponse;
import com.avocat.dto.RegisterRequest;
import com.avocat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Sécurité : On ne peut pas s'inscrire comme Admin via le formulaire public
        if ("ADMINISTRATEUR".equalsIgnoreCase(request.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Action non autorisée");
        }
        try {
            String message = authService.register(request);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Retourne une erreur 401 si le compte est EN_ATTENTE ou mot de passe faux
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}