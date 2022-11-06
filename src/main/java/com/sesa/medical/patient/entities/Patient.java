package com.sesa.medical.patient.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sesa.medical.sos.entities.Sos;
import com.sesa.medical.users.entities.Users;
import lombok.*;

import javax.persistence.*;
import java.util.Collection;

@Entity
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString
public class Patient extends Users {

    private String matricule;

    @OneToMany(mappedBy = "patient")
    private Collection<Abonnement> abonnements;
    @OneToMany(mappedBy = "patient")
    @JsonIgnore
    Collection<Sos> sosList;
    @OneToOne(mappedBy = "patient")
    private Carnet carnet;

    @ManyToOne
    StatusAccount statusAccount;

    public Patient(String username, String email, String password) {
        super(username, email, password);
    }
}
