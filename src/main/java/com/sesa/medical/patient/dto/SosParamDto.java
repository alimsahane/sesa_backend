package com.sesa.medical.patient.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SosParamDto {
    private double taille;
    private double poids;
    private double temperature;
    private double frequenceCardiaque;
    private double pouls;
    private double frequenceRespiratoire;
    private double saturationOxygene;
    private double perimetreBranchial;
}
