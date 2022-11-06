package com.sesa.medical.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class EmailVerificationDto extends EmailSenderDto implements Serializable {
    String code;
}
