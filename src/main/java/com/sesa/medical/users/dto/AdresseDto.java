package com.sesa.medical.users.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AdresseDto {

    private String street;
    @NotNull(message = "{postalCode.required}")
    private int postalCode;

    private String town;

    private String country;

    private String quater;
    @NotNull(message = "{latitude.required}")
    private String latitude;
    @NotNull(message = "{longitude.required}")
    private String longitude;

}
