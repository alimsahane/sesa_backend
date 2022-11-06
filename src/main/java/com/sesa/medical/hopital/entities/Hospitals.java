package com.sesa.medical.hopital.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.patient.entities.Abonnement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Hospitals {
    @Schema(description = "identifiant unique de l'hopital", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    @NotNull
    private String name;
    private String latitude;
    private String longitude;
    @Column(columnDefinition = "TEXT")
    private String images;
    @Column(columnDefinition = "TEXT")
    private String description;
    private boolean isDelete = false;
    @ManyToMany
    Set<Department> departments ;

    @OneToMany(mappedBy = "hospitals")
    @JsonIgnore
    Collection<Abonnement> abonnements;
    @OneToMany(mappedBy = "hospitals")
    @JsonIgnore
    Collection<Doctors> doctors;

    public Hospitals(String name, String latitude, String longitude, String images, String description) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.images = images;
        this.description = description;
    }
}
