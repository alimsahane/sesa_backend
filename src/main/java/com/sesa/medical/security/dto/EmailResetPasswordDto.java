package com.sesa.medical.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class EmailResetPasswordDto extends EmailSenderDto implements Serializable {
    String username;
    String password;
}
