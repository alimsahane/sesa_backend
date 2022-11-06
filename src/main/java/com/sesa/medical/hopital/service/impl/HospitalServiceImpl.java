package com.sesa.medical.hopital.service.impl;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.hopital.repository.IHospitalRepo;
import com.sesa.medical.hopital.service.IHospitalService;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.medecin.repository.IDoctorsRepo;
import com.sesa.medical.patient.entities.Abonnement;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.patient.repository.IAbonnementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class HospitalServiceImpl implements IHospitalService {
    @Autowired
    IHospitalRepo hospitalRepo;
    @Autowired
    IDoctorsRepo doctorsRepo;
    @Autowired
    IAbonnementRepo abonnementRepo;
    @Override
    public Hospitals createHospital(Hospitals hopHospitals) {
        return hospitalRepo.save(hopHospitals);
    }

    @Override
    public Hospitals updateHospital(Hospitals hospitals, Long id) {
        Hospitals hospi = hospitalRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hospital where id: " + id+ "not exist"));
        hospi.setImages(hospitals.getImages());
        hospi.setDescription(hospitals.getDescription());
        hospi.setName(hospitals.getName());
        hospi.setLongitude(hospitals.getLongitude());
        hospi.setLatitude(hospitals.getLatitude());
        return hospitalRepo.save(hospi);
    }

    @Override
    public Page<Doctors> findDoctorsByHospital(Long id, int page, int size, String sort, String sortOrder) {
        Hospitals hospitals = getOneHospital(id);
        Sort sorting =Sort.by (Sort.Direction.fromString(sortOrder), sort);
        Pageable pageable = PageRequest.of(page,size,sorting);
        return doctorsRepo.findDistinctByIsDeleteFalseAndHospitalsOrderByIsActiveDesc(hospitals,pageable);
    }

    @Override
    public Page<Abonnement> findAbonnementByHospital(Long id, int page, int size, String sort, String sortBy) {
        Hospitals hospitals = getOneHospital(id);
        Sort sorting =Sort.by (Sort.Direction.fromString(sortBy), sort);
        Pageable pageable = PageRequest.of(page,size,sorting);
        return abonnementRepo.findByHospitals(hospitals,pageable);
    }

    @Override
    public boolean checkIfHospitalExist(String name) {
        return hospitalRepo.existsByNameAndIsDeleteFalse(name);
    }

    @Override
    public boolean exisbyId(Long id) {
        return hospitalRepo.existsByIdAndIsDeleteFalse(id);
    }

    @Override
    public Hospitals deleteHospital(Long id) {
        Hospitals hospitals = getOneHospital(id);
        hospitals.setDelete(true);
        return hospitalRepo.save(hospitals);
    }

    @Override
    public Hospitals restoreHospital(Long id) {
        Hospitals hospitals = getOneHospital(id);
        hospitals.setDelete(false);
        return hospitalRepo.save(hospitals);
    }

    @Override
    public void deleteDefinitiveHospital(Long id) {
        Hospitals hospitals = getOneHospital(id);
        hospitalRepo.delete(hospitals);
    }

    @Override
    public Page<Hospitals> getAllHospitalActive(int page, int size, String sort, String sortOrder) {
        Sort sorting =Sort.by (Sort.Direction.fromString(sortOrder), sort);
        Pageable pageable = PageRequest.of(page,size,sorting);
        return hospitalRepo.findByIsDeleteFalse(pageable);
    }

    @Override
    public Page<Hospitals> getAllHospital(int page, int size, String sort, String sortOrder) {
        Sort sorting =Sort.by (Sort.Direction.fromString(sortOrder), sort);
        Pageable pageable = PageRequest.of(page,size,sorting);
        return hospitalRepo.findAll(pageable);
    }

    @Override
    public List<Patient> getAllPatientByHospital(Long id_hospital) {
        List<Patient> list = new ArrayList<>();
        Hospitals hospitals = getOneHospital(id_hospital);
        List<Abonnement> abonnements = abonnementRepo.findByHospitals(hospitals);
        abonnements.forEach(abo -> {
            list.add(abo.getPatient());
        });
        return list;
    }

    Hospitals  getOneHospital(Long id) {
      Hospitals hospitals = hospitalRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hospital where id: "+ id + " not exist"));
      return hospitals;
  }
}
