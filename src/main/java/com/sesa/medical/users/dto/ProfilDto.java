package com.sesa.medical.users.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@ToString
public class ProfilDto {
    //@NotNull(message = "{firstName.required}")
    private String firstName;
  //  @NotNull(message = "{lastName.required}")
    private String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @Schema(description = "date de naissance", example = "dd-MM-yyyy", required = true)
  //  @NotNull(message = "{birthdate.required}")
    private LocalDate birthdate;
 //   @NotNull(message = "{birthdatePlace.required}")
    private String birthdatePlace;
    @Schema(description = "sexe de l'utilisateur", example = "M , F", required = true)
    @Size(max = 1)
   // @NotNull(message = "{sexe.required}")
    private String sexe;
   // @NotNull(message = "{maritalStatus.required}")
    private String maritalStatus;
   // @NotNull(message = "{nationality.required}")
    private String nationality;
   // @Pattern(regexp = "^\\d{9}$", message = "{phone.number}")
    private String tel2;

}
