package com.sesa.medical.patient.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Parametre {
    @Schema(description = "identifiant unique du parametrage", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    private double taille;
    private double poids;
    private double temperature;
    private double frequenceCardiaque;
    private double pouls;
    private double frequenceRespiratoire;
    private double saturationOxygene;
    private double perimetreBranchial;
    private boolean etat;
    private LocalDateTime createdAt;
    private LocalDateTime updateAd;
    @ManyToOne
    @JsonIgnore
    private  Carnet carnet;

}
