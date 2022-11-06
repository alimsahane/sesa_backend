package com.sesa.medical.security.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class TokenRefreshDto {
    @NotNull(message = "{refresh.required}")
    private String refreshToken;
}
