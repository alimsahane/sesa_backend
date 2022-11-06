package com.sesa.medical.patient.repository;

import com.sesa.medical.patient.entities.Carnet;
import com.sesa.medical.patient.entities.Parametre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;



public interface IParametreRepo extends JpaRepository<Parametre,Long> {
    Parametre findByCarnetAndEtatTrue(Carnet carnet);
    Parametre findByEtatTrue();
    Page<Parametre> findByCarnet(Carnet carnet, Pageable pageable);
}
