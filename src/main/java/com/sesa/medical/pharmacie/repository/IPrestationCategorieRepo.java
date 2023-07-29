package com.sesa.medical.pharmacie.repository;

import com.sesa.medical.pharmacie.entities.PrestationCategorie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPrestationCategorieRepo extends JpaRepository<PrestationCategorie,Long> {
}
