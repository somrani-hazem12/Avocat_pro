package com.avocat.repository;

import com.avocat.entity.SuiviAvocat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuiviAvocatRepository extends JpaRepository<SuiviAvocat, Long> {
    // Pour trouver les avocats d'un client
    List<SuiviAvocat> findByClientId(Long clientId);

    // Pour trouver les clients d'un avocat
    List<SuiviAvocat> findByAvocatId(Long avocatId);

    // Pour vérifier si un lien existe déjà (Gardé comme demandé)
    Optional<SuiviAvocat> findByClientIdAndAvocatId(Long clientId, Long avocatId);

    // On compte combien de clients suivent cet avocat
    long countByAvocatId(Long avocatId);

    // AJOUTÉ POUR FIXER LE PROBLÈME DE SUPPRESSION DES DOUBLONS
    // En utilisant "findAllBy...", Spring Data ramènera toutes les lignes en double sans planter.
    List<SuiviAvocat> findAllByClientIdAndAvocatId(Long clientId, Long avocatId);
}