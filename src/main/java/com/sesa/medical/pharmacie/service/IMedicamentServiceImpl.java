package com.sesa.medical.pharmacie.service;

import com.sesa.medical.pharmacie.entities.Medicaments;
import com.sesa.medical.pharmacie.entities.PrestationCategorie;
import com.sesa.medical.pharmacie.entities.PrestationDetailsCategories;
import com.sesa.medical.pharmacie.repository.IMedicamentRepo;
import com.sesa.medical.pharmacie.repository.IPrestationCategorieRepo;
import com.sesa.medical.pharmacie.repository.IPrestationDetailsCategories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class IMedicamentServiceImpl implements IMedicamentService {
    @Autowired
    IMedicamentRepo medicamentRepo;

    @Autowired
    IPrestationCategorieRepo prestationCategorieRepo;

    @Autowired
    IPrestationDetailsCategories prestationDetailsCategories;

    @Override
    public Medicaments getById(Long id) {
        return medicamentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("le médicament donc l'id est: "+id+" n'existe pas"));
    }

    @Override
    public Medicaments addmedicament(Medicaments pharmacie) {
        return medicamentRepo.save(pharmacie);
    }

    @Override
    public Medicaments updateStatus(boolean status, long pharmacieId) {
        Medicaments medoc = getById(pharmacieId);
        medoc.setEtat(status);
        return medicamentRepo.save(medoc);
    }

    @Override
    public Medicaments updatePharmacie(Medicaments pharmacie, long pharmacieId) {
        Medicaments medoc = getById(pharmacieId);
        medoc.setDecription(pharmacie.getDecription());
        medoc.setAmount(pharmacie.getAmount());
        medoc.setQuantite(pharmacie.getQuantite());
        return medicamentRepo.save(medoc);
    }

    @Override
    public List<Medicaments> getAllMedoc() {
        return medicamentRepo.findAll();
    }

    @Override
    public List<PrestationCategorie> getAllPrestationCategorie() {
        return prestationCategorieRepo.findAll();
    }

    @Override
    public List<PrestationDetailsCategories> getAllPrestationDetailsCategories() {
        return prestationDetailsCategories.findAll();
    }

    @Override
    public List<PrestationDetailsCategories> getAllPrestationByCategorie(Long categorieId) {
        PrestationCategorie prestationCategorie = prestationCategorieRepo.findById(categorieId).orElseThrow(() -> new ResourceNotFoundException("Categorie where id: " + categorieId + " not found"));
        return prestationDetailsCategories.findByPrestationCategorieOrderByPriceAsc(prestationCategorie);

    }


    @Override
    public void deleteMedoc(Long id) {
        Medicaments medoc = getById(id);
        medicamentRepo.delete(medoc);
    }
}
