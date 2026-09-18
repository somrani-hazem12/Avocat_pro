package com.avocat.service;

import com.avocat.dto.LoginRequest;
import com.avocat.dto.LoginResponse;
import com.avocat.dto.RegisterRequest;
import com.avocat.entity.Client;
import com.avocat.entity.Utilisateur;
import com.avocat.repository.ClientRepository;
import com.avocat.repository.UtilisateurRepository;
import com.avocat.security.JwtUtil;
import io.micrometer.core.instrument.Counter; // <-- AJOUT
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    // ---------- AJOUT : les 3 compteurs de metriques ----------
    private final Counter connexionsReussiesCounter;
    private final Counter connexionsEchoueesCounter;
    private final Counter comptesBloquesCounter;

    @Transactional
    public String register(RegisterRequest request) {
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé !");
        }

        Utilisateur user = new Utilisateur();
        user.setNom(request.getNom());
        user.setEmail(request.getEmail());
        user.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        user.setRole(request.getRole());
        user.setTelephone(request.getTelephone());
        user.setAdresse(request.getAdresse());
        user.setStatut("EN_ATTENTE");
        user.setEchecsConnexion(0);

        Utilisateur savedUser = utilisateurRepository.save(user);

        if ("CLIENT".equals(request.getRole())) {
            Client clientProfile = new Client();
            clientProfile.setNom(savedUser.getNom());
            clientProfile.setEmail(savedUser.getEmail());
            clientProfile.setTelephone(savedUser.getTelephone());
            clientProfile.setAdresse(savedUser.getAdresse());
            clientProfile.setUtilisateur(savedUser);
            clientRepository.save(clientProfile);
        }

        auditService.enregistrerLog("INSCRIPTION", request.getEmail(), "Nouvelle inscription en attente de validation");
        return "Inscription réussie !";
    }

    @Transactional(noRollbackFor = RuntimeException.class)
    public LoginResponse login(LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect !"));

        if ("BLOQUE".equals(user.getStatut())) {
            auditService.enregistrerLog("SÉCURITÉ_ALERTE", user.getEmail(), "Tentative de connexion sur compte BLOQUÉ");
            connexionsEchoueesCounter.increment(); // <-- AJOUT
            throw new RuntimeException("Ce compte est verrouillé suite à trop d'échecs. Contactez l'administrateur.");
        }

        if ("EN_ATTENTE".equals(user.getStatut())) {
            throw new RuntimeException("Votre compte n'a pas encore été validé par l'administrateur.");
        }

        if (!passwordEncoder.matches(request.getMotDePasse(), user.getMotDePasse())) {
            int nouveauxEchecs = user.getEchecsConnexion() + 1;
            user.setEchecsConnexion(nouveauxEchecs);

            if (nouveauxEchecs >= 5) {
                user.setStatut("BLOQUE");
                auditService.enregistrerLog("SÉCURITÉ_VERROUILLAGE", user.getEmail(), "Compte BLOQUÉ après 5 échecs");
                utilisateurRepository.saveAndFlush(user);
                connexionsEchoueesCounter.increment(); // <-- AJOUT
                comptesBloquesCounter.increment();      // <-- AJOUT
                throw new RuntimeException("Compte bloqué après 5 tentatives infructueuses.");
            } else {
                utilisateurRepository.saveAndFlush(user);
                auditService.enregistrerLog("ÉCHEC_CONNEXION", user.getEmail(), "Mauvais MDP. Tentative n°" + nouveauxEchecs);
                connexionsEchoueesCounter.increment(); // <-- AJOUT
                throw new RuntimeException("Email ou mot de passe incorrect ! Tentatives restantes : " + (5 - nouveauxEchecs));
            }
        }

        user.setEchecsConnexion(0);
        utilisateurRepository.saveAndFlush(user);
        auditService.enregistrerLog("CONNEXION_RÉUSSIE", user.getEmail(), "Utilisateur connecté avec succès");
        connexionsReussiesCounter.increment(); // <-- AJOUT

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getRole(), user.getNom(), user.getId());
    }
}