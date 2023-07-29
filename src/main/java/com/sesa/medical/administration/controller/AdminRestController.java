package com.sesa.medical.administration.controller;

import com.sesa.medical.administration.dto.PasswordResetDto;
import com.sesa.medical.globalconfig.ApplicationConstant;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.medecin.repository.IDoctorsRepo;
import com.sesa.medical.medecin.service.IDoctorService;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.security.dto.MessageResponseDto;
import com.sesa.medical.users.entities.Adresses;
import com.sesa.medical.users.entities.Users;
import com.sesa.medical.users.repository.IUsersRepository;
/*import com.sesa.medical.users.services.IDocumentStorageService;*/
import com.sesa.medical.users.services.IUserService;
import com.sesa.medical.utilities.service.UtilitieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Administration")
@RequestMapping("/api/v1.0/admin")
@Slf4j
public class AdminRestController {

    @Autowired
    UtilitieService utilitieService;

    @Autowired
    IUserService userService;

    @Autowired
    IDoctorService doctorService;

    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    ModelMapper modelMapper;

    /*@Autowired
    IDocumentStorageService documentStorageService;*/

    @Autowired
    IUsersRepository usersRepository;

    @Autowired
    IPatientService patientService;

    @Autowired
    IDoctorsRepo doctorsRepo;

    @Autowired
    JmsTemplate jmsTemplate;

    @Operation(summary = "Supprimer le compte d'un utilisateur", tags = "Administration", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("hasRole('ADMIN') AND @authorizationService.canUpdateOwnerItem(#userId,'User')")
    @DeleteMapping("/{userId:[0-9]+}")
    public ResponseEntity<Object> deleteCustomer(@PathVariable Long userId) {
        Users user = userService.deleteUser(userId);
        if (user.isDelete()) {
            return ResponseEntity.ok(new MessageResponseDto(HttpStatus.OK, "Utilisateur supprimé avec succès"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDto(HttpStatus.OK, "Erreur lors de la suppresion"));
    }

    @Operation(summary = "Liste paginée des médecins", tags = "Administration", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Doctors.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/doctor")
    public ResponseEntity<Page<Users>> getAllDoctorPaginate(
            @Parameter(description = "number of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "size of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "sort of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "firstName") String sortBy) {
        Page<Users> page = doctorService.getAllDoctorPagination(pageNo, pageSize, sortBy);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @Operation(summary = "Liste paginée de tous les utilisateurs", tags = "Administration", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Users.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<Page<Users>> getAllUsersPaginate(
            @Parameter(description = "number of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "size of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "sort of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "firstName") String sortBy) {
        Page<Users> page = userService.getAllUsers(pageNo, pageSize, sortBy);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @Operation(summary = "Liste paginée de tous les patients de la plateforme", tags = "Administration", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Patient.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/patient")
    public ResponseEntity<Page<Patient>> getAllPatient(
            @Parameter(description = "number of the page to be find. Cannot be empty.",
                    required = false) @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "size of the page to be find. Cannot be empty.",
                    required = false) @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "sort of the page to be find. Cannot be empty.",
                    required = false) @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order) {
        Page<Patient> page = patientService.getListPaginatePatient(pageNo, pageSize, sortBy,order);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @Operation(summary = "Bloquer ou activer le compte d'un utilisateur", tags = "Administration", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content(mediaType = "Application/Json")), })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/lockAndUnlockAccount/{id}/{status}")
    public ResponseEntity<?> downloadFile(@PathVariable("id") Long userId,@PathVariable boolean status)  {
        userService.lockAndUnlockUsers(userId,status);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponseDto(HttpStatus.OK, messageSource
                .getMessage("messages.user_status_account_update", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Ajouter un médecin à un hôpital spécifique", tags = "Administration", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content(mediaType = "Application/Json")), })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/addDoctor/{id_doctor}/toHospital/{id_hospital}")
    public ResponseEntity<?> downloadFile(@PathVariable("id_doctor") Long id_doctor,@PathVariable("id_hospital") Long id_hospital)  {
        Doctors doctors = doctorService.addDoctorToHospital(id_doctor,id_hospital);
        return ResponseEntity.status(HttpStatus.OK).body(doctors);
    }


    @Operation(summary = "Ajouter un médecin à un hôpital spécifique", tags = "Administration", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content(mediaType = "Application/Json")), })
   // @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sendPasswordDoctor")
    public ResponseEntity<?> sendPasswordDoictor() {
        List<Doctors> doctorsList = doctorsRepo.findAll();
        doctorsList.forEach(d -> {
            Map<String, Object> userAndPasswordNotEncoded = new HashMap<>();
            userAndPasswordNotEncoded = doctorService.updatePasswordDoctorAndSendEmail(d);
            Doctors doc = (Doctors) userAndPasswordNotEncoded.get("users");
            String password = (String) userAndPasswordNotEncoded.get("password");
            PasswordResetDto jms = new PasswordResetDto();
            jms.setCompletName(doc.getFirstName()+" "+doc.getLastName());
            jms.setUsername(doc.getEmail());
            jms.setPassword(password);
            jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_EMAIL_RESETPASSWORD_DOCTOR, jms);
        });
       return ResponseEntity.ok("mot de passe de tous les médecin réinitialisé avec succès");
    }






}
