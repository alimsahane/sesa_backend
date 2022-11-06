package com.sesa.medical.security.dto;

import lombok.Value;

@Value
public class SignInResponseOtp {
    private boolean using2FA;
    private String message;
    private String access_token;
    private boolean authenticated;
}
