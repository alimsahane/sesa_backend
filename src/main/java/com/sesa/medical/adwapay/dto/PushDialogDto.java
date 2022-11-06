package com.sesa.medical.adwapay.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PushDialogDto {
    @NotNull(message = "adpFootprint obligatoire")
    private String adpFootprint;
    @NotNull(message = "tokenCode obligatoire")
    private String tokenCode;
}
