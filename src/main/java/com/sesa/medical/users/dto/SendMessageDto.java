package com.sesa.medical.users.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SendMessageDto {
    @NotNull(message = "{senderId.required}")
    private Long senderId;
    @NotNull(message = "{receiverId.required}")
    private Long receiverId;
    @NotNull(message = "{message.required}")
    private String message;

    private String fcmToken;
}
