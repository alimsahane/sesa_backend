package com.sesa.medical.packsesa.service;

import com.sesa.medical.packsesa.entities.Categorie;
import com.sesa.medical.packsesa.entities.PackSesa;
import com.sesa.medical.patient.entities.Abonnement;
import com.sesa.medical.patient.entities.Payement;

import java.util.List;

public interface IPackSesaService {

    Categorie create(Categorie categorie);

    PackSesa createPack(PackSesa packSesa, Long id_cat);

    Abonnement createAbonnement(Abonnement abo, Long id_patient, Long id_pack,Long id_hospital);

    Payement createPay(Payement payement, Long id_abo, Long id_mode);

    List<Categorie> getAllCategorie();

    List<PackSesa> getAllPackByCategorie(Long categorieId);

    PackSesa getById(Long id_pack);


}
