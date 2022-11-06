package com.sesa.medical.users.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "mail")
public class MailPropertiesDto {
    public static class SMTP {
        String host;
        String port;
        List<String> username = new ArrayList<>();
        List<String> password= new ArrayList<>();

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public List<String> getUsername() {
            return username;
        }

        public void setUsername(List<String> username) {
            this.username = username;
        }

        public List<String> getPassword() {
            return password;
        }

        public void setPassword(List<String> password) {
            this.password = password;
        }
    }

    private SMTP smtp;
    private List<String> from= new ArrayList<>();
    private String fromName;
    private String verificationapi;
    private String forgotpassword;

    public SMTP getSmtp() {
        return smtp;
    }

    public void setSmtp(SMTP smtp) {
        this.smtp = smtp;
    }

    public List<String> getFrom() {
        return from;
    }

    public void setFrom(List<String> from) {
        this.from = from;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getVerificationapi() {
        return verificationapi;
    }

    public void setVerificationapi(String verificationapi) {
        this.verificationapi = verificationapi;
    }

    public String getForgotpassword() {
        return forgotpassword;
    }

    public void setForgotpassword(String forgotpassword) {
        this.forgotpassword = forgotpassword;
    }

}
