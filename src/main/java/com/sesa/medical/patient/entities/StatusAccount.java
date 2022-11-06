package com.sesa.medical.patient.entities;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString
@Table(name = "StatusAccount")
public class StatusAccount {

    @Schema(description = "identifiant unique du status de compte", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @Schema(description = "nom du status de compte", example = "STANDARD, MEMBER")
    @Enumerated(EnumType.STRING)
    private EStatusAccount name;


    public StatusAccount(EStatusAccount name) {
        this.name = name;
    }
}
