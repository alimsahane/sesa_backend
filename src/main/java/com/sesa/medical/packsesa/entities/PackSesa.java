package com.sesa.medical.packsesa.entities;


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
public class PackSesa {

    @Schema(description = "identifiant unique du carnet", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String acronyme;

    private int price;

    private int duration;

    private String description;

    @ManyToOne()
    @JoinColumn(name = "categorie_id")
    Categorie categorie;


    @OneToMany(mappedBy = "packSesa")
    @JsonIgnore
    List<Abonnement> abonnements = new ArrayList<>();
}
