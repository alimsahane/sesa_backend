package com.sesa.medical.packsesa.service;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.hopital.repository.IHospitalRepo;
import com.sesa.medical.packsesa.entities.Categorie;
import com.sesa.medical.packsesa.entities.PackSesa;
import com.sesa.medical.packsesa.repository.ICategorieRepository;
import com.sesa.medical.packsesa.repository.IPackSesaRepository;
import com.sesa.medical.patient.entities.Abonnement;
import com.sesa.medical.patient.entities.ModePay;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.patient.entities.Payement;
import com.sesa.medical.patient.repository.IAbonnementRepo;
import com.sesa.medical.patient.repository.IModeRepo;
import com.sesa.medical.patient.repository.IPayementRepo;
import com.sesa.medical.patient.service.IPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PackSesaServiceImpl implements IPackSesaService {

    @Autowired
    IAbonnementRepo abonnementRepo;

    @Autowired
    IPayementRepo payementRepo;

    @Autowired
    ICategorieRepository categorieRepo;

    @Autowired
    IPackSesaRepository packSesaRepo;

    @Autowired
    IPatientService patientService;

    @Autowired
    IHospitalRepo hospitalRepo;

    @Autowired
    IModeRepo modeRepo;

    @Override
    public Categorie create(Categorie categorie) {
        return categorieRepo.save(categorie);
    }

    @Override
    public PackSesa createPack(PackSesa packSesa, Long id_cat) {
        Categorie categorie = categorieRepo.findById(id_cat).orElseThrow(() -> new ResourceNotFoundException("Categorie where id: " + id_cat + " not found"));
        packSesa.setCategorie(categorie);
        return packSesaRepo.save(packSesa);
    }

    @Override
    public Abonnement createAbonnement(Abonnement abo, Long id_patient, Long id_pack, Long id_hospital) {
        Patient pat = patientService.getOnePatient(id_patient);
        PackSesa packSesa = packSesaRepo.findById(id_pack).orElseThrow(() -> new ResourceNotFoundException("Pack where id: " + id_pack + " not found"));
        Hospitals hospitals = hospitalRepo.findById(id_hospital).orElseThrow(() -> new ResourceNotFoundException("Hospital where id: " + id_hospital + " not found"));
        abo.setHospitals(hospitals);
        abo.setPatient(pat);
        abo.setPackSesa(packSesa);
        return abonnementRepo.save(abo);
    }

    @Override
    public Payement createPay(Payement payement, Long id_abo, Long id_mode) {
        Abonnement abonnement = abonnementRepo.findById(id_abo).orElseThrow(() -> new ResourceNotFoundException("Abonnement where id: " + id_abo + " not found"));
        ModePay mode = modeRepo.findById(id_mode).orElseThrow(() -> new ResourceNotFoundException("Mode payement where id: " + id_mode + " not found"));
        payement.setModePay(mode);
        payement.setAbonnement(abonnement);
        return payementRepo.save(payement);
    }

    @Override
    public List<Categorie> getAllCategorie() {
        return categorieRepo.findAll();
    }

    @Override
    public List<PackSesa> getAllPackByCategorie(Long categorieId) {
        Categorie categorie = categorieRepo.findById(categorieId).orElseThrow(() -> new ResourceNotFoundException("Categorie where id: " + categorieId + " not found"));
        return packSesaRepo.findByCategorieOrderByPriceAsc(categorie);
    }

    @Override
    public PackSesa getById(Long id_pack) {
        PackSesa packSesa = packSesaRepo.findById(id_pack).orElseThrow(() -> new ResourceNotFoundException("Pack where id: " + id_pack + " not found"));
        return packSesa;
    }
}
