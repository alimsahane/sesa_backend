package com.sesa.medical.prestation.repositoty;

import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.prestation.entities.PaiementPresta;
import com.sesa.medical.prestation.entities.Prestation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestationRepo extends JpaRepository<Prestation,Long> {

    List<Prestation> findByPatient(Patient patient);

    Prestation findByPaiementPresta(PaiementPresta paie);
}
