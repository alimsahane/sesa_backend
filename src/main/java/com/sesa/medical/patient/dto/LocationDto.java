package com.sesa.medical.patient.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class LocationDto {
    @NotNull(message = "${latitudePatient.required}")
    @NotBlank(message = "${latitudePatient.required}")
    private String latitude;
    @NotNull(message = "${longitudePatient.required}")
    @NotBlank(message = "${longitudePatient.required}")
    private String longitude;
    private String  name;
    private String message;

}
