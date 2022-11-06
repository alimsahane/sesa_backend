package com.sesa.medical.callbackservice;

import com.sesa.medical.callbackservice.dto.EmailDto;

public interface IEmailService {
    void sendEmail(EmailDto emailDto);
}
