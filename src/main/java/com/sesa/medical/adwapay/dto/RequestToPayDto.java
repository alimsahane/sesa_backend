package com.sesa.medical.adwapay.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RequestToPayDto {
    @NotNull(message = "userId obligatoire")
    private Long userId;
    @NotNull(message = "modePayId obligatoire")
    private Long modePayId;
    @NotNull(message = "packId obligatoire")
    private Long packId;
    @NotNull(message = "tokenCode obligatoire")
    private String tokenCode;
    @NotNull(message = "paymentNumber obligatoire")
    private String paymentNumber;
    @NotNull(message = "commisssionAmoun obligatoire")
    private double commisssionAmount;

}
