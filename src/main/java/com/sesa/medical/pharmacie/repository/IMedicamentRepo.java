package com.sesa.medical.pharmacie.repository;

import com.sesa.medical.pharmacie.entities.Medicaments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IMedicamentRepo extends JpaRepository<Medicaments,Long> {
}
