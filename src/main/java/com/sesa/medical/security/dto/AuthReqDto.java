package com.sesa.medical.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthReqDto {
	@NotNull(message = "${username.required}")
	private String username;

	@Email(message = "{email.valid}")
	private String email;

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
	@Pattern(regexp = "^[0-9+\\(\\)#\\.\\s\\/ext-]+$", message = "{phone.number}")
	private String tel;
}
