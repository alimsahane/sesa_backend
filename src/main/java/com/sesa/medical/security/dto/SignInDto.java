package com.sesa.medical.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignInDto {

    @NotNull(message = "${username.required}")
    private String username;

    @Size(min = 6,message = "{password.lenght}")
    @NotNull(message = "{password.required}")
    /*@Pattern.List({
            @Pattern(regexp = "(?=.*[0-9]).+", message = "{password.number}")
            ,
            @Pattern(regexp = "(?=.*[a-z]).+", message = "{password.lowercase}")
            ,
            @Pattern(regexp = "(?=.*[A-Z]).+", message = "{password.upercase}")
            ,
            @Pattern(regexp = "(?=.*[!@#$%^&*+=?-_()/\"\\.,<>~`;:]).+", message = "{password.capitalletter}")
            ,
            @Pattern(regexp = "(?=\\S+$).+", message = "{password.spacer}")})*/
    private String password;
    @Schema(required = true, allowableValues = {"mobile", "web"})
    @NotNull(message = "{appProvider.required}")
    private String appProvider  ;
}
