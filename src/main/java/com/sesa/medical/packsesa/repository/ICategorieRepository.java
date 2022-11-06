package com.sesa.medical.packsesa.repository;

import com.sesa.medical.packsesa.entities.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategorieRepository extends JpaRepository<Categorie,Long> {
}
