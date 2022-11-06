package com.sesa.medical.callbackservice;

import com.sesa.medical.callbackservice.dto.EmailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
@Service
@Slf4j
public class EmailServiceImpl implements IEmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Override
    public void sendEmail(EmailDto emailDto) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariables(emailDto.getProps());
            String html = templateEngine.process(emailDto.getBody(), context);

            mimeMessageHelper.setFrom(emailDto.getFrom(), emailDto.getSenderName());
            mimeMessageHelper.setTo(emailDto.getTo());
            mimeMessageHelper.setSubject(emailDto.getSubject());
            mimeMessageHelper.setReplyTo(emailDto.getReplyTo(), emailDto.getReplyToName());
            mimeMessageHelper.setText(html, true);
            mimeMessageHelper.addInline("facebook", new ClassPathResource("static/facebook.png"));
            mimeMessageHelper.addInline("linkedin", new ClassPathResource("static/linkedin.png"));
            mimeMessageHelper.addInline("instagram", new ClassPathResource("static/instagram.png"));
            mimeMessageHelper.addInline("youtube", new ClassPathResource("static/youtube.png"));
            emailSender.send(mimeMessage);
            log.info("Email Successful send to {}", emailDto.getTo());
        } catch (Exception ex) {
            log.error("Email send fail to {} error occurred {}", emailDto.getTo(), ex.getLocalizedMessage());
        }
    }

}
