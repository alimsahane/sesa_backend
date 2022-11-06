package com.sesa.medical.users.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;


import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Adresses {
    @Schema(description = "identifiant unique de l'adresse", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long Id;
    private String street;

    @Column(nullable = true)
    private int postalCode;

    private String town;

    private String country;

    private String quater;
    private String latitude;
    private String longitude;
    @OneToOne()
    @JoinColumn(name="users_id",nullable = false)
    @JsonIgnore
    private  Users users;

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }
}
