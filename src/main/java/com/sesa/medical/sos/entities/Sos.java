package com.sesa.medical.sos.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.patient.entities.Patient;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sos {


    @Schema(description = "identifiant unique de l'abonnement", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String message;
    private LocalDateTime createdAt;
    private boolean etat = true;
    private String providerName;
    private boolean statusEnvois= false;
    @ManyToOne
    @JsonIgnore
    Patient patient;
    @ManyToOne
    @JsonIgnore
    Doctors doctors;
}
