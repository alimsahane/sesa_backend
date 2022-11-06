package com.sesa.medical.prestation.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sesa.medical.patient.entities.ModePay;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaiementPresta {

    @Schema(description = "identifiant de la description", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private boolean statusPay;

    private String payNumber;

    private String montant;

    private String idTransaction;

    private LocalDateTime createdAt;

    @OneToOne()
    @JsonIgnore
    Prestation prestation;

    @ManyToOne
    private ModePay modePay;
}
