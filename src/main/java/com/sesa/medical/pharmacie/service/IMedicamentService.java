package com.sesa.medical.pharmacie.service;

import com.sesa.medical.pharmacie.entities.Medicaments;
import com.sesa.medical.pharmacie.entities.PrestationCategorie;
import com.sesa.medical.pharmacie.entities.PrestationDetailsCategories;

import java.util.List;

public interface IMedicamentService {

    Medicaments getById(Long id);

    Medicaments addmedicament(Medicaments pharmacie);

    Medicaments updateStatus(boolean status, long pharmacieId);

    Medicaments updatePharmacie(Medicaments pharmacie, long pharmacieId);

    List<Medicaments> getAllMedoc();

    List<PrestationCategorie> getAllPrestationCategorie();
    List<PrestationDetailsCategories> getAllPrestationDetailsCategories();

    List<PrestationDetailsCategories> getAllPrestationByCategorie(Long categorieId);

    void deleteMedoc(Long id);


}
