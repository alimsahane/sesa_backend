package com.sesa.medical.pharmacie.repository;


import com.sesa.medical.pharmacie.entities.PrestationCategorie;
import com.sesa.medical.pharmacie.entities.PrestationDetailsCategories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPrestationDetailsCategories extends JpaRepository<PrestationDetailsCategories,Long> {
    List<PrestationDetailsCategories> findByPrestationCategorieOrderByPriceAsc(PrestationCategorie prestationCategorie);

}
