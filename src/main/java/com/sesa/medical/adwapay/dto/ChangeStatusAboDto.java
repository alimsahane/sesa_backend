package com.sesa.medical.adwapay.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ChangeStatusAboDto {
    @NotNull(message = "transactionId obligatoire")
    private  String transactionId;
}
