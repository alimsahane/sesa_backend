package com.sesa.medical.patient.repository;

import com.sesa.medical.patient.entities.Abonnement;
import com.sesa.medical.patient.entities.Payement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPayementRepo extends JpaRepository<Payement,Long> {
    Payement findByIdTransaction(String transactionId);
    Payement findByAbonnement(Abonnement abonnement);
}
