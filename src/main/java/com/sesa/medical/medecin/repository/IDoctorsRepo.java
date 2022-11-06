package com.sesa.medical.medecin.repository;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.users.entities.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IDoctorsRepo extends JpaRepository<Doctors, Long> {
    Page<Users> findDistinctByIsDeleteFalseOrderByIsActiveDesc(Pageable pageable);
    Page<Doctors> findDistinctByIsDeleteFalseAndHospitalsOrderByIsActiveDesc(Hospitals hospitals, Pageable pageable);
    List<Doctors> findDistinctByHospitalsAndSosReceivedTrue(Hospitals hospitals);

}
