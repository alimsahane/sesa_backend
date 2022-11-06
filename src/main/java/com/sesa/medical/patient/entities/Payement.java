package com.sesa.medical.patient.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payement {


    @Schema(description = "identifiant unique du mode de payement", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    @Column(unique = true)
    private  String idTransaction;
    private double amount;
    private Date createdAt;
    private Date updateAt;
    private boolean etat;

    @OneToOne()
    @JoinColumn(name = "abonnement_id")
    @JsonIgnore
    Abonnement abonnement;

    @ManyToOne
    private ModePay modePay;
}
