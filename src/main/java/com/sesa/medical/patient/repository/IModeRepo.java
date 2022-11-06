package com.sesa.medical.patient.repository;

import com.sesa.medical.patient.entities.ModePay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IModeRepo extends JpaRepository<ModePay,Long> {
    List<ModePay> findByDeleteFalse();

}
