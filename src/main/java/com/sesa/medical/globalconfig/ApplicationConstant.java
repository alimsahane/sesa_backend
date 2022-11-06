package com.sesa.medical.globalconfig;

import javax.servlet.http.HttpServletRequest;

public class ApplicationConstant {
    public static final String SUBJECT_EMAIL_VERIF = "SeSa Email Verification";

    public static final String SUBJECT_PASSWORD_RESET = "SeSa Password Reset";

    public static final String SUBJECT_SOS = "SOS SOS";

    public static final String TEMPLATE_EMAIL_VERIF = "email-verification.html";

    public static final String TEMPLATE_PASSWORD_RESET = "email-password-reset.html";

    public static final String TEMPLATE_EMAIL_ACCESS_MEDECIN = "doctor-access";

    public static final String TEMPLATE_SOS = "email-sos.html";

    public static final String PRODUCER_EMAIL_VERIFICATION = "producer.email.verification" ;

    public static final String PRODUCER_EMAIL_RESET_PASSWORD = "producer.email.reset.password" ;

    public static final String PRODUCER_EMAIL_SOS = "producer.email.sos" ;

    public static final String PRODUCER_SMS_SOS = "producer.sms.sos" ;

    public static final String PRODUCER_SMS_PAYPRESTA_SUCCESSFULL = "producer.sms.pay.presta" ;

    public static final String PRODUCER_SMS_OTP = "producer.sms.otp" ;

    public static final String PRODUCER_PUSH_SOS = "producer.push.notification" ;

    public static final String PRODUCER_INTERNAL_SOS = "producer.internal.sos" ;

    public static final String PRODUCER_EMAIL_RESETPASSWORD_DOCTOR = "producer.resetpassword.doctor";

    public static String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

}
