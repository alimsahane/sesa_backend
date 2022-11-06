package com.sesa.medical.users.dto;

import com.sesa.medical.security.dto.AuthProvider;
import com.sesa.medical.users.entities.ERoles;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class AddUserDto {
    @NotNull(message = "{username.required}")
    private String username;

    @Email(message = "{email.valid}")
    private String email;

    @Schema(description = "provider de l'utilisateur", example = "local, facebook, google, github")
    @NotNull(message = "{provider.required}")
    private String providerName;
    private boolean using2FA;
}
