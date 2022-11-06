package com.sesa.medical.callbackservice;

import com.sesa.medical.adwapay.dto.PrestapayDtoJms;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.patient.dto.SosDtoJms;
import com.sesa.medical.patient.entities.Abonnement;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.patient.service.impl.PatientService;
import com.sesa.medical.security.dto.OtpCodeDto;
import com.sesa.medical.users.services.imp.SendingSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class CallbackServiceSMS {
    @Autowired
    SendingSmsService sendingSmsService;

    @Autowired
    PatientService patientService;

    @JmsListener(destination = "producer.sms.sos")
    public void callBackAfterSosNotification(SosDtoJms sosDtoJms) throws IOException {
      //  sendingSmsService.sendSmsPersoPrestaSinch(sosDtoJms);
        sendingSmsService.sendsmspersoToPrestaMboadeal("SOS SeSa",sosDtoJms.getReceiver().getToPhone(),sosDtoJms.getMessage());
        System.out.println("emailResetPassword: " + sosDtoJms.toString());
        log.info("Email sos sent successful to user:  " + sosDtoJms.getReceiver().getToName());
    }

    @JmsListener(destination = "producer.sms.otp")
    public void callBackAfterOtpCodeSend(OtpCodeDto otpCodeDto) throws IOException {
        //sendingSmsService.sendSmsOtpCodePersoPrestaSinch(otpCodeDto);
        sendingSmsService.sendsmspersoToPrestaMboadeal("SeSa Auth",otpCodeDto.getTel(),"code OTP : " + URLEncoder.encode(otpCodeDto.getCode(), String.valueOf(StandardCharsets.UTF_8)));
        System.out.println("otp code sent: " + otpCodeDto.toString());
        log.info("otp code sent successful, code:  " + otpCodeDto.getCode());
    }

    @JmsListener(destination = "producer.sms.pay.presta")
    public void callBackAfterSosNotification(PrestapayDtoJms jms) throws IOException {
        Patient patient = patientService.getOnePatient(jms.getUserId());
        Abonnement abonnement = patientService.findAbonnementByPatient(patient);
        List<Doctors> doctors = patientService.findDoctorByHospitalAndSosreceiveTrue(abonnement.getHospitals());
        String receiverlist = doctors.stream().map(s -> s.getTel1() !=null ? s.getTel1(): s.getTel2()).collect(Collectors.joining(","));
        jms.setReceiverNumber(receiverlist);
        String message = "Paiement d'une prestation avec succès : " + "\n"
                + "Nom :" + jms.getCompletName() + "\n"
                + "orderNumber: " + jms.getPhone() + "\n"
                + "amount: " + jms.getAmount() + "\n"
                + "mode pay: " + jms.getModePay()+ "\n"
                + "Date pay" + jms.getDate()+ "\n"
                + "presta description: " + jms.getDescription();
        sendingSmsService.sendsmspersoToPrestaMboadeal("Presta Pay",jms.getReceiverNumber(),message);
        // sendingSmsService.sendSmsAfterPayPresta(jms);
        System.out.println("jms template: " + jms.toString());
        log.info("Sms pay sent successful to user:  " + jms.getReceiverNumber());
    }

}
