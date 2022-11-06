package com.sesa.medical.adwapay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddFeePrestaDto {

    @Schema(description = "montant de la prestation à payer", example ="1")
    @NotNull(message = "montant de la prestation obligatoire")
    private int amount;
    @NotNull(message = "tokenCode obligatoire")
    private String tokenCode;
}
