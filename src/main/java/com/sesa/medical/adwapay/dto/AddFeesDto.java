package com.sesa.medical.adwapay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AddFeesDto {

    @Schema(description = "Identifiant unique d'un pack sesa", example ="1")
    @NotNull(message = "packId obligatoire")
    private Long packId;
    @NotNull(message = "tokenCode obligatoire")
    private String tokenCode;
}
