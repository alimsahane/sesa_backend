package com.sesa.medical.packsesa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackSesaDto {

    private String acronyme;

    private int price;

    private int duration;

    private String description;

    private Long categorieId;
}
