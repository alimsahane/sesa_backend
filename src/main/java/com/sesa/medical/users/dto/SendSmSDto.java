package com.sesa.medical.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSmSDto {

    private String senderid;

    private String mobiles;

    private String sms;

    private String user;

    private String password;
}
