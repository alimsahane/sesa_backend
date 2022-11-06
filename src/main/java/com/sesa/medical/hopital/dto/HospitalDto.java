package com.sesa.medical.hopital.dto;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
public class HospitalDto {
    @NotBlank(message = "{nameHospital.required}")
    private String name;
    @NotBlank(message = "{latitudeHospital.required}")
    private String latitude;
    @NotBlank(message = "{longitudeHospital.required}")
    private String longitude;

    private String images;

    private String description;
}
