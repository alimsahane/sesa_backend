package com.sesa.medical.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthRefreshResp {
    private String bearerToken;
    private String tokenType = "Bearer";

    public AuthRefreshResp(String bearerToken) {
        this.bearerToken = bearerToken;
    }
}
