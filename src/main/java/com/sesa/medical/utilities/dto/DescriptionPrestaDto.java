package com.sesa.medical.utilities.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DescriptionPrestaDto {
    @NotNull(message = "description de prestation obligatoire")
    private String description;
}
