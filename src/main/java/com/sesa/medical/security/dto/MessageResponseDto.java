package com.sesa.medical.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MessageResponseDto {
	
	private HttpStatus status;
    private String message;
    private List<String> errors;
    
    public MessageResponseDto(HttpStatus status, String message, String error) {
        super();
        this.status = status;
        this.message = message;
        errors = Arrays.asList(error);
    }
    
    public MessageResponseDto(HttpStatus status, String message) {
        super();
        this.status = status;
        this.message = message;
    }

    public MessageResponseDto(String message) {
        super();
        this.message = message;
    }
}
