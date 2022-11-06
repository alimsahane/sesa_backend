package com.sesa.medical.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSmSDto {

    private String sender_name;

    private String phone_str;

    private String message;

    private String user_id;

    private String password;
}
