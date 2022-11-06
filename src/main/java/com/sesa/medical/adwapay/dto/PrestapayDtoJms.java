package com.sesa.medical.adwapay.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PrestapayDtoJms implements Serializable {
    private String description;

    private String amount;

    private String phone;

    private String date;

    private String modePay;

    private String receiverNumber;

    private String completName;

    private Long userId;
}
