package com.sesa.medical.callbackservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
public class EmailDto {
    private String to;

    private String subject;

    private String body;

    private String replyTo;

    private String from;

    private String senderName;

    private String replyToName;

    private MultipartFile[] files;

    private Map<String, Object> props = new HashMap<>();

    public EmailDto(String from,String senderName, String to,String replyTo, Map<String, Object> props, String subject, String template) {
        super();
        this.from = from;
        this.senderName = senderName;
        this.to = to;
        this.replyTo= replyTo;
        this.subject = subject;
        this.body = template;
        this.props = props;
    }
}
