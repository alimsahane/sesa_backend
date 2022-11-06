package com.sesa.medical.users.controller;


import com.pusher.rest.Pusher;
import com.sesa.medical.medecin.service.IDoctorService;
import com.sesa.medical.patient.entities.Chat;
import com.sesa.medical.patient.entities.EStatusMessage;
import com.sesa.medical.patient.entities.EmessageType;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.security.dto.MessageResponseDto;
import com.sesa.medical.users.dto.*;
import com.sesa.medical.users.entities.Adresses;
import com.sesa.medical.users.entities.Users;
import com.sesa.medical.users.repository.IUsersRepository;
import com.sesa.medical.users.services.IDocumentStorageService;
import com.sesa.medical.users.services.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "users")
@RequestMapping("/api/v1.0/user")
@Slf4j
public class UserRestController {
    @Autowired
    IUserService userService;
    @Autowired
    IDoctorService doctorService;
    @Autowired
    private ResourceBundleMessageSource messageSource;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    IDocumentStorageService documentStorageService;
    @Autowired
    IUsersRepository usersRepository;

    @Autowired
    IPatientService patientService;

    @Value("${app.api.base.url}")
    private String api_base_url;

    @Operation(summary = "Supprimer le compte d'un utilisateur", tags = "users", responses = {
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

    @Operation(summary = "Liste paginée des médecins", tags = "users", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Users.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    /*@PreAuthorize("hasRole('ADMIN')")*/
    @GetMapping("/doctor")
    public ResponseEntity<Page<Users>> getAllDoctorPaginate(
            @Parameter(description = "number of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "size of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "200") int pageSize,
            @Parameter(description = "sort of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "isActive") String sortBy) {
        Page<Users> page = doctorService.getAllDoctorPagination(pageNo, pageSize, sortBy);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @Operation(summary = "Changer la disponibilitée d'un médecin", tags = "users", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Users.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAllDoctorPaginate(@PathVariable Long doctorId,@RequestParam  boolean available) {
        doctorService.activeAndDesactiveDoctore(doctorId,available);
        return ResponseEntity.ok(new MessageResponseDto(HttpStatus.OK, messageSource
                .getMessage("messages.doctor_available", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Liste paginée de tous les utilisateurs", tags = "users", responses = {
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

    @Operation(summary = "recupérer l'adresse d'un utilisateur", tags = "users", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Adresses.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
  //  @PreAuthorize("@authorizationService.canUpdateOwnerItem(#id,'User')")
    @GetMapping("/adresse/{id}")
    public ResponseEntity<?> getUsersAdresse(@PathVariable Long id) {
        Adresses adresses = userService.getAdresseUser(id);
        if (adresses == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDto(HttpStatus.NOT_FOUND, messageSource.getMessage("messages.adresse_not_exists", null, LocaleContextHolder.getLocale())));
        }
        return new ResponseEntity<>(adresses, HttpStatus.OK);
    }

    @Operation(summary = "Modification du profil d'un utilisateur", tags = "users", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = ProfilDto.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    //@PreAuthorize("@authorizationService.canUpdateOwnerItem(#id,'User')")
    @PutMapping("/updateprofil/{id}")
    public ResponseEntity<?> updateProfil(@Valid @RequestBody ProfilDto profilDto, @PathVariable Long id) {
        if (userService.existsByTel2(profilDto.getTel2(), id)) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("messages.phone_exists2", null, LocaleContextHolder.getLocale())));
        }
        Users user = modelMapper.map(profilDto, Users.class);
        Users usersave = userService.updateProfil(user, id);
        return new ResponseEntity<>(modelMapper.map(usersave, ProfilDto.class), HttpStatus.CREATED);
    }

    @Operation(summary = "creation de l'adresse d'un utilisateur", tags = "users", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Adresses.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
 //   @PreAuthorize("@authorizationService.canUpdateOwnerItem(#id,'User')")
    @PostMapping("/adresse/{id}")
    public ResponseEntity<?> saveAdresseUser(@Valid @RequestBody AdresseDto adresseDto, @PathVariable Long id) {
        Adresses adresses = modelMapper.map(adresseDto, Adresses.class);
        Users users = userService.getById(id);
        if(users.getAdresses()!=null) {
            return  ResponseEntity.badRequest().body("Cet utilisateur dispose déjà d'une adresse");
        }
        Adresses save = userService.createAdresseUser(adresses, id);
        return new ResponseEntity<>(save, HttpStatus.CREATED);
    }

    @Operation(summary = "modification  de l'adresse d'un utilisateur", tags = "users", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Adresses.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "500", description = "this user already has an address", content = @Content(mediaType = "Application/Json")),})
    @PutMapping("/adresse/update/{id}")
    public ResponseEntity<?> updateAdresseUser(@Valid @RequestBody AdresseDto adresseDto, @PathVariable Long id) {
        Adresses adresses = modelMapper.map(adresseDto, Adresses.class);
        Adresses save = userService.updateAdresseUser(adresses, id);
        return new ResponseEntity<>(save, HttpStatus.CREATED);
    }

    @Parameters(value = {
            @Parameter(name = "docType", schema = @Schema(allowableValues = {"profile", "other"}))})
    @Operation(summary = "Téléverser un document ", tags = "users", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = UploadFileResponseDto.class)))),
            @ApiResponse(responseCode = "500", description = "Sorry! Filename contains invalid path sequence / Could not store file", content = @Content(mediaType = "Application/Json"))})
    @PostMapping("/{id:[0-9]+}/uploadFile")
    public UploadFileResponseDto uploadFile(@RequestBody MultipartFile file, @PathVariable("id") Long userId, @RequestParam("docType") String docType) {
        String fileName = documentStorageService.storeFile(file, userId, docType);
        String fileDownloadUri = api_base_url+"/api/v1.0/user/file/" + userId + "/downloadFile?docType=" + docType;
        if (docType.equals("profile")) {
            Users users = userService.getById(userId);
            users.setImageUrl(fileDownloadUri);
            usersRepository.save(users);
        }
        return new UploadFileResponseDto(fileName, fileDownloadUri, file.getContentType(), file.getSize());

    }

    @Operation(summary = "Télécharger un document", tags = "users", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content(mediaType = "Application/Json")),})
    @GetMapping("/file/{id:[0-9]+}/downloadFile")
    public ResponseEntity<Object> downloadFile(@PathVariable("id") Long userId, @RequestParam String docType,
                                               HttpServletRequest request) {
        String fileName = documentStorageService.getDocumentName(userId, docType);
        Resource resource = null;
        if (fileName != null && !fileName.isEmpty()) {
            try {
                resource = documentStorageService.loadFileAsResource(fileName);
            } catch (Exception e) {
                log.info(e.getMessage());
            }
            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException e) {
                log.info("Could not determine file type.");
            }
            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
    }

    @Operation(summary = "création et modification  du fcmToken (firabase)  d'un utilisateur", tags = "users", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Adresses.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "500", description = "this user already has an address", content = @Content(mediaType = "Application/Json")),})
    @PutMapping("/fcmtoken/{id}")
    public ResponseEntity<?> updateAdresseUser(@Valid @RequestBody AddFcmToken addFcmToken, @PathVariable Long id) {
        Users users = userService.updateFcmToken(addFcmToken.getFcmToken(),id);
        return new ResponseEntity<>(users, HttpStatus.CREATED);
    }


    @Operation(summary = "Bloquer ou activer le compte d'un utilisateur", tags = "users", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content(mediaType = "Application/Json")), })
    @GetMapping("/lockAndUnlockAccount/{id}/{status}")
    public ResponseEntity<?> downloadFile(@PathVariable("id") Long userId,@PathVariable boolean status)  {
        userService.lockAndUnlockUsers(userId,status);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponseDto(HttpStatus.OK, messageSource
                .getMessage("messages.user_status_account_update", null, LocaleContextHolder.getLocale())));
    }


}
