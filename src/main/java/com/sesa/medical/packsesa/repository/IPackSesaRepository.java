package com.sesa.medical.packsesa.repository;

import com.sesa.medical.packsesa.entities.Categorie;
import com.sesa.medical.packsesa.entities.PackSesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPackSesaRepository extends JpaRepository<PackSesa,Long> {

    List<PackSesa> findByCategorieOrderByPriceAsc(Categorie categorie);
}
