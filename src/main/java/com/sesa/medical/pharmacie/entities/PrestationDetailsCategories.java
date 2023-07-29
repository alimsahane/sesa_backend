package com.sesa.medical.pharmacie.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sesa.medical.patient.entities.Abonnement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.validator.constraints.EAN;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrestationDetailsCategories {
    @Schema(description = "identifiant unique du carnet", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String nom;

    private int price;
    @ManyToOne()
    @JoinColumn(name = "prestation_categorie_id")
    PrestationCategorie prestationCategorie;
}
