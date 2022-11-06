package com.sesa.medical.medecin.entities;

import com.sesa.medical.hopital.entities.Department;
import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.hopital.entities.SubSpeciality;
import com.sesa.medical.users.entities.Users;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString
public class Doctors extends Users {
    boolean sosReceived = false;
    private String matricule;
    @ManyToOne
    private Hospitals hospitals;
    @ManyToOne
    private Department department;
    @ManyToOne
    private SubSpeciality speciality;

    @Column(columnDefinition = "boolean default false")
    private Boolean  isActive;


    public Doctors(String username, String email, String password) {
        super(username, email, password);
    }
}
