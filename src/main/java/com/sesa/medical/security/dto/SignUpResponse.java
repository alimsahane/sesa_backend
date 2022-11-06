package com.sesa.medical.security.dto;

import lombok.Value;

import java.util.List;

@Value
public class SignUpResponse {
    private boolean using2FA;
    private String qrCodeImage;
    private String access_token;
    private boolean authenticated;
}
