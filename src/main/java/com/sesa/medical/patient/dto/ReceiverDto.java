package com.sesa.medical.patient.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReceiverDto {
    Long id;
    String toName;
    String toEmail;
    String toPhone;
}
