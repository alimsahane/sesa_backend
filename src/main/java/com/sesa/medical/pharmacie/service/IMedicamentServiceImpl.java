package com.sesa.medical.pharmacie.service;

import com.sesa.medical.pharmacie.entities.Medicaments;
import com.sesa.medical.pharmacie.repository.IMedicamentRepo;
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
    public void deleteMedoc(Long id) {
        Medicaments medoc = getById(id);
        medicamentRepo.delete(medoc);
    }
}
