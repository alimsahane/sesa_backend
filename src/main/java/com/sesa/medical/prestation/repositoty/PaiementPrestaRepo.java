package com.sesa.medical.prestation.repositoty;

import com.sesa.medical.prestation.entities.PaiementPresta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementPrestaRepo extends JpaRepository<PaiementPresta,Long> {
    PaiementPresta findByIdTransaction(String transactionId);
}
