package com.avocat.controller;

import com.avocat.entity.Utilisateur;
import com.avocat.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ClientController {

    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/avocats-disponibles")
    public List<Utilisateur> getAvocatsDisponibles() {
        return utilisateurRepository.findAllByRole("AVOCAT");
    }
}