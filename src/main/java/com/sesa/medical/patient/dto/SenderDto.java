package com.sesa.medical.patient.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class SenderDto  implements Serializable {
    Long id;
    String fromName;
    String fromEmail;
    String  fromPhone;
}
