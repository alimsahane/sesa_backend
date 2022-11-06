package com.sesa.medical.patient.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.packsesa.entities.PackSesa;
import com.sesa.medical.users.entities.Users;
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
public class Abonnement {

    @Schema(description = "identifiant unique de l'abonnement", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    private double amount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDate createdAt;
    private LocalDate updateAt;
    private boolean etat = true;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    @JsonIgnore
    private Patient patient;
    @ManyToOne
    @JoinColumn(name = "hopital_id")
    private Hospitals hospitals;

    @OneToOne(mappedBy = "abonnement")
    private Payement payement;

    @ManyToOne()
    @JoinColumn(name = "pack_sesa_id")
    PackSesa packSesa;


}
