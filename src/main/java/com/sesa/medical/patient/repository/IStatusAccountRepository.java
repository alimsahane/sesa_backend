package com.sesa.medical.patient.repository;


import com.sesa.medical.patient.entities.EStatusAccount;
import com.sesa.medical.patient.entities.StatusAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IStatusAccountRepository extends JpaRepository<StatusAccount,Long> {
    StatusAccount findByName(EStatusAccount eStatusAccount);
}
