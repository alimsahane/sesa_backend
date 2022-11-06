package com.sesa.medical.callbackservice;

import com.sesa.medical.administration.dto.PasswordResetDto;
import com.sesa.medical.callbackservice.dto.EmailDto;
import com.sesa.medical.globalconfig.ApplicationConstant;
import com.sesa.medical.patient.dto.SosDtoJms;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.security.dto.EmailResetPasswordDto;
import com.sesa.medical.security.dto.EmailVerificationDto;
import com.sesa.medical.sos.entities.Sos;
import com.sesa.medical.users.services.imp.SendingMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class CallbackServiceEmail {
    @Autowired
    SendingMailService sendingMailService;
    @Autowired
    IPatientService patientService;

    @Autowired
    IEmailService emailService;

    @JmsListener(destination = "producer.email.verification")
    public void callbackAfterEmailVerification(EmailVerificationDto emailVerificationDto) {
     sendingMailService.sendVerificationMail(emailVerificationDto);
        System.out.println("emailVerifieDto: "+emailVerificationDto.toString());
        log.info("Email vérification send successfull for user" + emailVerificationDto.getTo());
    }

    @JmsListener(destination = "producer.email.reset.password")
    public void callbackAfterEmailResetPassword(EmailResetPasswordDto emailResetPasswordDto) {
     sendingMailService.sendVerificationMail(emailResetPasswordDto);
        System.out.println("emailResetPassword: "+emailResetPasswordDto.toString());
        log.info("Email reset-password send successfull for user" + emailResetPasswordDto.getTo());
    }

    @JmsListener(destination = "producer.resetpassword.doctor")
    public void callbackAfterEmailResetPasswordDoctor(PasswordResetDto jms) {
        Map<String, Object> emailProps = new HashMap<>();
        emailProps.put("completname", jms.getCompletName());
        emailProps.put("username", jms.getUsername());
        emailProps.put("password", jms.getPassword());
        emailService.sendEmail(new EmailDto("moudjiefabrice9@gmail.com", "SeSa.Auth", jms.getUsername(), "", emailProps,"Accès temporaire pour médecin", ApplicationConstant.TEMPLATE_EMAIL_ACCESS_MEDECIN));
        System.out.println("jms template: " + jms.toString());
        log.info("Email send successfull for doctor" + jms.getUsername());
    }

    @JmsListener(destination= "producer.email.sos")
    public void callBackAfterSosNotification (SosDtoJms sosDtoJms) {
    boolean statusSend = sendingMailService.sendMailSos(sosDtoJms);
       Sos sos = new Sos();
    if(statusSend == true) {
        sos.setEtat(true);
        sos.setStatusEnvois(true);
    }else {
        sos.setEtat(false);
        sos.setStatusEnvois(false);
    }
    sos.setProviderName("email");
    sos.setMessage(sosDtoJms.getMessage() +"poids: " +sosDtoJms.getPoids()+" taille: "+sosDtoJms.getTaille()+"température: "+sosDtoJms.getTemperature()+ " poul: "+sosDtoJms.getPouls());
        patientService.CreateSosPatient(sos,sosDtoJms.getSender().getId(),sosDtoJms.getReceiver().getId());
        System.out.println("emailResetPassword: "+sosDtoJms.toString());
        log.info("Email sos sent successful to user:  " + sosDtoJms.getReceiver().getToEmail());
    }
}
