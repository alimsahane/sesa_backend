package com.sesa.medical.hopital.service;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.patient.entities.Abonnement;
import com.sesa.medical.patient.entities.Patient;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IHospitalService {
 Hospitals createHospital(Hospitals hopHospitals);
 Hospitals updateHospital(Hospitals hospitals, Long id);
 Page<Doctors> findDoctorsByHospital(Long id,int page,int size,String sort,String sortOrder);
 Page<Abonnement> findAbonnementByHospital(Long id, int page,int size,String sort,String sortBy);
 boolean checkIfHospitalExist(String name);
 boolean exisbyId(Long id);
 Hospitals deleteHospital(Long id);
 Hospitals restoreHospital(Long id);
 public void deleteDefinitiveHospital(Long id);
 Page<Hospitals> getAllHospitalActive(int page , int size, String sort,String sortOrder);
 Page<Hospitals> getAllHospital(int page , int size, String sort,String sortOrder);
 List<Patient> getAllPatientByHospital(Long id_hospital);
}
