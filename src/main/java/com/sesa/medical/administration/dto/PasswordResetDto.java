package com.sesa.medical.administration.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Getter
@Setter
@ToString
public class PasswordResetDto implements Serializable {

    private String completName;

    private String username;

    private String password;
}
