package com.sesa.medical.patient.repository;

import com.sesa.medical.patient.entities.Carnet;
import com.sesa.medical.patient.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICarnetRepo extends JpaRepository<Carnet,Long> {
    Carnet findByPatient(Patient p);
}
