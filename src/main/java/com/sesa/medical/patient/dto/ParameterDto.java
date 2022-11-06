package com.sesa.medical.patient.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ParameterDto {
    @NotNull(message = "${taillePatient.required}")
    @NotBlank(message = "${taillePatient.required}")
    private double taille;
    @NotNull(message = "${poidPatient.required}")
    @NotBlank(message = "${poidPatient.required}")
    private double poids;
    private double temperature;
    private double frequenceCardiaque;
    @NotNull(message = "${poulsPatient.required}")
    @NotBlank(message = "${poulsPatient.required}")
    private double pouls;
    private double frequenceRespiratoire;
    private double saturationOxygene;
    private double perimetreBranchial;
}
