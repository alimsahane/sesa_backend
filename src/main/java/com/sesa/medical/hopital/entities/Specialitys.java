package com.sesa.medical.hopital.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Specialitys {
    @Schema(description = "identifiant unique de la spécialité", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long Id;
    @NotNull
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;

}
