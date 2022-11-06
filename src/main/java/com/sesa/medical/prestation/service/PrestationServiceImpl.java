package com.sesa.medical.prestation.service;

import com.sesa.medical.patient.entities.ModePay;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.prestation.entities.DescriptionPrestation;
import com.sesa.medical.prestation.entities.PaiementPresta;
import com.sesa.medical.prestation.entities.Prestation;
import com.sesa.medical.prestation.repositoty.DescriptionPrestaRepo;
import com.sesa.medical.prestation.repositoty.PaiementPrestaRepo;
import com.sesa.medical.prestation.repositoty.PrestationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PrestationServiceImpl implements PrestationService {
    @Autowired
    IPatientService patientService;

    @Autowired
    PrestationRepo prestationRepo;

    @Autowired
    PaiementPrestaRepo paiementPrestaRepo;

    @Autowired
    DescriptionPrestaRepo descriptionPrestaRepo;

    @Override
    public DescriptionPrestation createDescription(DescriptionPrestation descriptionPresta) {
        descriptionPresta.setCreatedAt(LocalDateTime.now());
        return descriptionPrestaRepo.save(descriptionPresta);
    }

    @Override
    public List<DescriptionPrestation> getAllDescriptionPresta() {
        return descriptionPrestaRepo.findAll();
    }

    @Override
    public DescriptionPrestation updateDescription(DescriptionPrestation desc, Long descId) {
        DescriptionPrestation descUp = descriptionPrestaRepo.findById(descId).orElseThrow(() -> new ResourceNotFoundException("la descriotion dont l'id est :" + descId + " n'existe pas"));
        descUp.setDescription(desc.getDescription());
        return descriptionPrestaRepo.save(descUp);
    }

    @Override
    public Page<DescriptionPrestation> getDescriptionPaginate(int page, int size, String sort, String order) {
        return null;
    }

    @Override
    public void deleteDescription(Long id) {
        DescriptionPrestation desc = descriptionPrestaRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("la descriotion dont l'id est :" + id + " n'existe pas"));
        descriptionPrestaRepo.delete(desc);
    }

    @Override
    public Prestation createPrestation(Prestation prestation, Long patientId) {
        Patient patient = patientService.getOnePatient(patientId);
        prestation.setPatient(patient);
        return prestationRepo.save(prestation);
    }

    @Override
    public Prestation updateStatusPrestation(Long prestaId, boolean status) {
        Prestation presta = prestationRepo.findById(prestaId).orElseThrow(() -> new ResourceNotFoundException("la prestation dont l'id est :" + prestaId + " n'existe pas"));
        presta.setStatusPay(status);
        return prestationRepo.save(presta);
    }

    @Override
    public List<Prestation> getListPrestationPatient(Long patientId) {
        Patient patient = patientService.getOnePatient(patientId);
        return prestationRepo.findByPatient(patient);
    }

    @Override
    public PaiementPresta createPaypresta(PaiementPresta paie, Long prestaId,Long modeId) {
        ModePay mode = patientService.getOneMode(modeId);
        Prestation presta = prestationRepo.findById(prestaId).orElseThrow(() -> new ResourceNotFoundException("la prestation dont l'id est :" + prestaId + " n'existe pas"));
        paie.setPrestation(presta);
        paie.setModePay(mode);
        return paiementPrestaRepo.save(paie);
    }

    @Override
    public PaiementPresta updateStatus(Long idPay, boolean status) {
       PaiementPresta paie = paiementPrestaRepo.findById(idPay).orElseThrow(() -> new ResourceNotFoundException("le paiement dont l'id est :" + idPay + " n'existe pas"));
       paie.setStatusPay(status);
        return paiementPrestaRepo.save(paie);
    }
}
