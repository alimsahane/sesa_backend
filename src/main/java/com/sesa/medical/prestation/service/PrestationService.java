package com.sesa.medical.prestation.service;

import com.sesa.medical.prestation.entities.DescriptionPrestation;
import com.sesa.medical.prestation.entities.PaiementPresta;
import com.sesa.medical.prestation.entities.Prestation;
import com.sesa.medical.prestation.repositoty.DescriptionPrestaRepo;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PrestationService {

    DescriptionPrestation createDescription(DescriptionPrestation descriptionPresta);

    List<DescriptionPrestation > getAllDescriptionPresta();

    DescriptionPrestation updateDescription(DescriptionPrestation  desc, Long descId);

    Page<DescriptionPrestation > getDescriptionPaginate(int page , int size, String sort, String order);

    void deleteDescription(Long id);

    Prestation createPrestation(Prestation prestation, Long patientId);

    Prestation updateStatusPrestation(Long prestaId, boolean status);

    List<Prestation> getListPrestationPatient(Long patientId);

    PaiementPresta createPaypresta(PaiementPresta paie,Long prestaId,Long modeId);

    PaiementPresta updateStatus(Long idPay, boolean status);
}
