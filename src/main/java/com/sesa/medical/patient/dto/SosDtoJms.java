package com.sesa.medical.patient.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Getter
@Setter
@ToString
public class SosDtoJms extends SosParamDto implements Serializable {
    private SenderDto sender;
    private ReceiverDto receiver;
    private String message;
    private String latitude;
    private String longitude;
    private String name;
}
