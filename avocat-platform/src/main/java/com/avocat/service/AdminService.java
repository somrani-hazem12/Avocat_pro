package com.avocat.service;

import com.avocat.entity.Utilisateur;
import com.avocat.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UtilisateurRepository utilisateurRepository;

    public List<Utilisateur> getInscriptionsEnAttente() {
        return utilisateurRepository.findByStatut("EN_ATTENTE");
    }

    public void accepterUtilisateur(Long id) {
        Utilisateur user = utilisateurRepository.findById(id).orElseThrow();
        user.setStatut("ACTIF");
        user.setMotifRefus(null);
        utilisateurRepository.save(user);
    }

    public void refuserUtilisateur(Long id, String motif) {
        Utilisateur user = utilisateurRepository.findById(id).orElseThrow();
        user.setStatut("REFUSE");
        user.setMotifRefus(motif);
        utilisateurRepository.save(user);
    }

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", utilisateurRepository.count());
        stats.put("actifs", (long) utilisateurRepository.findByStatut("ACTIF").size());
        stats.put("enAttente", (long) utilisateurRepository.findByStatut("EN_ATTENTE").size());
        stats.put("avocats", (long) utilisateurRepository.findAllByRole("AVOCAT").size());
        stats.put("clients", (long) utilisateurRepository.findAllByRole("CLIENT").size());
        return stats;
    }

    public void deleteUser(Long id) {
        utilisateurRepository.deleteById(id);
    }

    public void updateUser(Long id, Utilisateur details) {
        Utilisateur user = utilisateurRepository.findById(id).orElseThrow();
        user.setNom(details.getNom());
        user.setEmail(details.getEmail());
        user.setRole(details.getRole());
        utilisateurRepository.save(user);
    }
}