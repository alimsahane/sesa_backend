package com.sesa.medical.users.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AddFcmToken {

    @NotNull(message = "{fcmToken.required}")
    private String fcmToken;
}
