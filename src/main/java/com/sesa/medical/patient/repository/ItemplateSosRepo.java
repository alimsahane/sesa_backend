package com.sesa.medical.patient.repository;

import com.sesa.medical.patient.entities.TemplateSos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemplateSosRepo extends JpaRepository<TemplateSos,Long> {
    TemplateSos findByEtatTrue();
}
