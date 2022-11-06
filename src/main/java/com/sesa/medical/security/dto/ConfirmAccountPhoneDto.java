package com.sesa.medical.security.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class ConfirmAccountPhoneDto {
    @NotNull(message = "${code.required}")
    private String code;
    @NotNull(message = "${phone.required}")
    //@Pattern(regexp = "^\\d{9}$", message = "{phone.number}")
    @Pattern(regexp = "^[0-9+\\(\\)#\\.\\s\\/ext-]+$", message = "{phone.number}")
    private String tel;
}
