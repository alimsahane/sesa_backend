package com.sesa.medical.users.services.imp;

import com.sesa.medical.globalconfig.ApplicationConstant;
import com.sesa.medical.patient.dto.SosDtoJms;
import com.sesa.medical.security.dto.EmailResetPasswordDto;
import com.sesa.medical.security.dto.EmailVerificationDto;
import com.sesa.medical.users.dto.MailPropertiesDto;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SendingMailService {
    private final MailPropertiesDto mailProperties;

    private final Configuration templates;



    @Autowired
    SendingMailService(MailPropertiesDto mailProperties, Configuration templates){
        this.mailProperties = mailProperties;
        this.templates = templates;
    }
    public boolean sendVerificationMail(EmailVerificationDto emailVerificationDto) {
        String subject = emailVerificationDto.getObject();
        String body = "";
        try {
            Template t = templates.getTemplate(emailVerificationDto.getTemplate());
            Map<String, String> map = new HashMap<>();
            map.put("VERIFICATION_URL",  emailVerificationDto.getCode());
            body = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);

        }
        return sendMail(emailVerificationDto.getTo(), subject, body);
    }

    public boolean sendVerificationMail(EmailResetPasswordDto emailResetPasswordDto) {
        String subject = emailResetPasswordDto.getObject();
        String body = "";
        try {
            Template t = templates.getTemplate(emailResetPasswordDto.getTemplate());
            Map<String, String> map = new HashMap<>();
            map.put("URL",  emailResetPasswordDto.getCode());
            map.put("USER_USERNAME", emailResetPasswordDto.getUsername());
            body = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);

        }
        return sendMail(emailResetPasswordDto.getTo(), subject, body);
    }

    public boolean sendMailSos(SosDtoJms sosDtoJms) {
        String subject = ApplicationConstant.SUBJECT_SOS;
        String body = sosDtoJms.getMessage();
        try {
            Template t = templates.getTemplate("email-sos.html");
            Map<String, String> map = new HashMap<>();
            map.put("param_poids",  String.valueOf(sosDtoJms.getPoids()));
            map.put("param_pouls",  String.valueOf(sosDtoJms.getPouls()));
            map.put("param_taille",  String.valueOf(sosDtoJms.getTaille()));
            map.put("param_temperature",  String.valueOf(sosDtoJms.getTemperature()));
            map.put("param_frecard",  String.valueOf(sosDtoJms.getFrequenceCardiaque()));
            map.put("param_freres",  String.valueOf(sosDtoJms.getFrequenceRespiratoire()));
            map.put("param_peri",  String.valueOf(sosDtoJms.getPerimetreBranchial()));
            map.put("param_sat",  String.valueOf(sosDtoJms.getSaturationOxygene()));
            body = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);

        }
        return sendMail(sosDtoJms.getReceiver().getToEmail(), subject, body);
    }

    private boolean sendMail(String toEmail, String subject, String body) {
        String address = null;
        String fromName = null;
        try {
            Properties props = System.getProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", mailProperties.getSmtp().getPort());
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.auth", "true");

            Session session = Session.getDefaultInstance(props);
            session.setDebug(true);

            MimeMessage msg = new MimeMessage(session);

            MimeMessageHelper helper = new MimeMessageHelper(msg,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            address = mailProperties.getFrom().get(0);
            fromName = mailProperties.getFromName();
            helper.setFrom(new InternetAddress(address, fromName));
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.addInline("mail", new ClassPathResource("static/mail.png"));
            helper.addInline("facebook", new ClassPathResource("static/facebook.png"));
            helper.addInline("twetter", new ClassPathResource("static/tweeter.png"));
            helper.addInline("instagram", new ClassPathResource("static/instagram.png"));
            helper.addInline("youtube", new ClassPathResource("static/youtube.png"));
            Transport transport = session.getTransport();
            transport.connect(mailProperties.getSmtp().getHost(), mailProperties.getSmtp().getUsername().get(0), mailProperties.getSmtp().getPassword().get(0));
            transport.sendMessage(msg, msg.getAllRecipients());
            return true;
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return false;
    }
}
