package com.sesa.medical.security.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class ResetOtpCodeDto {
    @NotNull(message = "${appProvider.required}")
    private String appProvider;
    @NotNull(message = "${phone.required}")
    @Pattern(regexp = "^[0-9+\\(\\)#\\.\\s\\/ext-]+$", message = "{phone.number}")
    private String tel;
}
