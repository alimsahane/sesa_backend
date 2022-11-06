package com.sesa.medical.hopital.repository;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.medecin.entities.Doctors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IHospitalRepo extends JpaRepository<Hospitals, Long> {
    boolean existsByNameAndIsDeleteFalse(String name);
    boolean existsByIdAndIsDeleteFalse(Long id);
    Page<Hospitals> findByIsDeleteFalse(Pageable pageable);


}
