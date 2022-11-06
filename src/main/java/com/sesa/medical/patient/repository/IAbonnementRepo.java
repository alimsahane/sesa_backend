package com.sesa.medical.patient.repository;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.patient.entities.Abonnement;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.patient.entities.Payement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IAbonnementRepo extends JpaRepository<Abonnement,Long> {

Page<Abonnement> findByHospitals(Hospitals hospitals, Pageable pageable);

 Optional<Abonnement> findByPatientAndEtatTrue(Patient patient);
 List<Abonnement> findByPatient(Patient patient);

 Optional<Abonnement> findByPatientAndEtatFalse(Patient patient);

 List<Abonnement>  findByHospitals(Hospitals hospitals);

 Abonnement findByPayement(Payement payement);
}
