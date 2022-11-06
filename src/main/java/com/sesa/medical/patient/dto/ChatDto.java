package com.sesa.medical.patient.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatDto {
    private Long id;
    private Long sender_id;
    private Long receiver_id;
    private String message;
    private String status_message;
    private String message_type;
    private String create_at;
}
