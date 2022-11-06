package com.sesa.medical.prestation.entities;

import com.sesa.medical.patient.entities.Patient;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@OnDelete(action = OnDeleteAction.CASCADE)
public class Prestation {

    @Schema(description = "identifiant de la description", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    private boolean statusPay;

    private String payNumber;

    private String montant;

    private LocalDateTime createdAt;

    @ManyToOne
    Patient patient;

    @OneToOne(mappedBy = "prestation")
    private PaiementPresta paiementPresta;
}
