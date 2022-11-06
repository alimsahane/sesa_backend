package com.sesa.medical.adwapay.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
@Getter
@Setter
public class RequestToPayPrestaDto {
    @NotNull(message = "userId obligatoire")
    private Long userId;
    @NotNull(message = "modePayId obligatoire")
    private Long modePayId;
    @NotNull(message = "montant de la prestation obligatoire")
    private int amount;

    @NotNull(message = "description de la prestation obligatoire")
    private String descriptionPresta;

    @NotNull(message = "tokenCode obligatoire")
    private String tokenCode;
    @NotNull(message = "paymentNumber obligatoire")
    private String paymentNumber;
    @NotNull(message = "commisssionAmoun obligatoire")
    private double commisssionAmount;
}
