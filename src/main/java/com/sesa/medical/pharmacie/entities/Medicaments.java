package com.sesa.medical.pharmacie.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Medicaments {
    @Schema(description = "identifiant unique du médicament ", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String decription;

    private int quantite;

    private boolean etat;

    private double amount;

    private String imageUrl;

    private LocalDateTime createdAt = LocalDateTime.now();
}
