package com.sesa.medical.users.dto;

import com.sesa.medical.security.dto.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserReqDto extends AddUserDto {

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

    @Schema(required = true, allowableValues = {"patient", "medecin"})
    @NotNull(message = "{userType.required}")
    private String userType;
    @Schema(required = true, allowableValues = {"email", "sms"})
    @NotNull(message = "{verificationType.required}")
    private String verificationType;
    @NotNull(message = "{tel1.required}")
    private String tel1;

    private String firstName;

    private String lastName;



}
