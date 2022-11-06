package com.sesa.medical.patient.repository;

import com.sesa.medical.patient.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPatientRepo extends JpaRepository<Patient, Long> {
}
