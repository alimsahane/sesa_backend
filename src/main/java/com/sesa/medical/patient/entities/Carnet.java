package com.sesa.medical.patient.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collection;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Carnet {
    @Schema(description = "identifiant unique du carnet", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    @Schema(description = "code unique du carnet", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Column(unique = true)
    private String code;
    private LocalDate createdAt;
    private LocalDate updateAt;
    private boolean etat = true;
    @OneToOne()
    @JoinColumn(name="patient_id",nullable = false)
    @JsonIgnore
    private Patient patient;
    @OneToMany(mappedBy = "carnet")
    private Collection<Parametre> parametres ;
}
