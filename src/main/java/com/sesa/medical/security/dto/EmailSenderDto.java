package com.sesa.medical.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class EmailSenderDto implements Serializable {
    private static final long serialVersionUID = 7526472295622776147L;
    private String to;
    private String object;
    private String message;
    private String template;
    private String code;
}
