package com.sesa.medical.security.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserResetPassword {
    @Size(min = 6,message = "{password.lenght}")
    @NotNull(message = "{password.required}")
    @Pattern.List({
            @Pattern(regexp = "(?=.*[0-9]).+", message = "{password.number}")
            ,
            @Pattern(regexp = "(?=.*[a-z]).+", message = "{password.lowercase}")
            ,
            @Pattern(regexp = "(?=.*[A-Z]).+", message = "{password.upercase}")
            ,
            @Pattern(regexp = "(?=.*[!@#$%^&*+=?-_()/\"\\.,<>~`;:]).+", message = "{password.capitalletter}")
            ,
            @Pattern(regexp = "(?=\\S+$).+", message = "{password.spacer}")})
    private String password;
    @NotNull(message = "${phone.required}")
    @Pattern(regexp = "^[0-9+\\(\\)#\\.\\s\\/ext-]+$", message = "{phone.number}")
    private String code;
    @NotNull(message = "${code.required}")
    private String tel;
}
