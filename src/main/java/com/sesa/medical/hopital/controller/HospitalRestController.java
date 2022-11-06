package com.sesa.medical.hopital.controller;


import com.sesa.medical.hopital.dto.HospitalDto;
import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.hopital.service.IHospitalService;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.medecin.service.IDoctorService;
import com.sesa.medical.patient.entities.Abonnement;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.security.dto.MessageResponseDto;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/hospital")
@Tag(name = "Hospital")
@Slf4j
public class HospitalRestController {
    @Autowired
    IHospitalService hospitalService;
    @Autowired
    private ResourceBundleMessageSource messageSource;
    @Autowired
    ModelMapper modelMapper;

    @Operation(summary = "creation d'un hopital", tags = "Hospital", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Hospitals.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping()
    public ResponseEntity<?> createHospital(@Valid @RequestBody HospitalDto hospitalDto) {
        if (hospitalService.checkIfHospitalExist(hospitalDto.getName())) {
            return ResponseEntity.badRequest().body(
                    new MessageResponseDto(HttpStatus.BAD_REQUEST, messageSource.getMessage("messages.name_exists", null, LocaleContextHolder.getLocale()))
            );
        }
        Hospitals hospitals = modelMapper.map(hospitalDto, Hospitals.class);
        return new ResponseEntity<>(hospitalService.createHospital(hospitals), HttpStatus.CREATED);
    }

    @Operation(summary = "modifier les informations d'un hopital", tags = "Hospital", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Hospitals.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHospital(@Valid @RequestBody HospitalDto hospitalDto, @PathVariable Long id) {
        Hospitals hospitalsave = null;
        if (hospitalService.exisbyId(id)) {
            Hospitals hos = modelMapper.map(hospitalDto, Hospitals.class);
            hospitalsave = hospitalService.updateHospital(hos, id);
        } else {
            return ResponseEntity.badRequest().body(
                    new MessageResponseDto(HttpStatus.BAD_REQUEST, messageSource.getMessage("messages.id_not_exists", null, LocaleContextHolder.getLocale()))
            );
        }
        return new ResponseEntity<>(hospitalsave, HttpStatus.CREATED);
    }

    @Parameters(value = {
            @Parameter(name = "sort", schema = @Schema(allowableValues = {"lastName", "birthdate", "birthdatePlace",
                    "sexe", "maritalStatus", "nationality"})),
            @Parameter(name = "order", schema = @Schema(allowableValues = {"asc", "desc"}))})
    @Operation(summary = "List des docteurs par hopital", tags = "Hospital", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Doctors.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER') OR hasRole('MEDECIN')")
    @GetMapping("/{id}/doctor")
    public ResponseEntity<Page<Doctors>> getAllDoctorsByHospitals(@RequestParam(required = false, value = "page", defaultValue = "0") int pageParam,
                                                                  @RequestParam(required = false, value = "size", defaultValue = "20") int sizeParam,
                                                                  @RequestParam(required = false, defaultValue = "userId") String sort,
                                                                  @RequestParam(required = false, defaultValue = "desc") String order,
                                                                  @PathVariable Long id) {
        return new ResponseEntity<>(hospitalService.findDoctorsByHospital(id, pageParam, sizeParam, sort, order), HttpStatus.OK);
    }


    @Operation(summary = "List des patients par hopital", tags = "Hospital", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Doctors.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/patients")
    public ResponseEntity<List<Patient>> getAllDoctorsByHospitals(@PathVariable Long id) {
        return new ResponseEntity<>(hospitalService.getAllPatientByHospital(id), HttpStatus.OK);
    }
    @Parameters(value = {
            @Parameter(name = "sort", schema = @Schema(allowableValues = {"name", "id"})),
            @Parameter(name = "order", schema = @Schema(allowableValues = {"asc", "desc"}))})
    @Operation(summary = "List paginée de tous les hopitaux", tags = "Hospital", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Hospitals.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping()
    public ResponseEntity<Page<Hospitals>> getAllHospitals(@RequestParam(required = false, value = "page", defaultValue = "0") int pageParam,
                                                                  @RequestParam(required = false, value = "size", defaultValue = "20") int sizeParam,
                                                                  @RequestParam(required = false, defaultValue = "id") String sort,
                                                                  @RequestParam(required = false, defaultValue = "desc") String order) {
        return new ResponseEntity<>(hospitalService.getAllHospital( pageParam, sizeParam, sort, order), HttpStatus.OK);
    }

    @Parameters(value = {
            @Parameter(name = "sort", schema = @Schema(allowableValues = {"name", "id"})),
            @Parameter(name = "order", schema = @Schema(allowableValues = {"asc", "desc"}))})
    @Operation(summary = "List paginée de tous les hopitaux actif", tags = "Hospital", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Hospitals.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN,ROLE_MEDECIN')")
    @GetMapping("/active")
    public ResponseEntity<Page<Hospitals>> getAllHospitalsActive(@RequestParam(required = false, value = "page", defaultValue = "0") int pageParam,
                                                           @RequestParam(required = false, value = "size", defaultValue = "20") int sizeParam,
                                                           @RequestParam(required = false, defaultValue = "id") String sort,
                                                           @RequestParam(required = false, defaultValue = "desc") String order) {
        return new ResponseEntity<>(hospitalService.getAllHospitalActive( pageParam, sizeParam, sort, order), HttpStatus.OK);
    }

    @Parameters(value = {
            @Parameter(name = "sort", schema = @Schema(allowableValues = {"amount", "startDate", "endDate",
                    "createdAt", "etat"})),
            @Parameter(name = "order", schema = @Schema(allowableValues = {"asc", "desc"}))})
    @Operation(summary = "List des abonnements par hopital", tags = "Hospital", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Abonnement.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/abonnement")
    public ResponseEntity<Page<Abonnement>> getAllAbonnementByHospitals(@RequestParam(required = false, value = "page", defaultValue = "0") int pageParam,
                                                                        @RequestParam(required = false, value = "size", defaultValue = "20") int sizeParam,
                                                                        @RequestParam(required = false, defaultValue = "id") String sort,
                                                                        @RequestParam(required = false, defaultValue = "desc") String order,
                                                                        @PathVariable Long id) {
        return new ResponseEntity<>(hospitalService.findAbonnementByHospital(id, pageParam, sizeParam, sort, order), HttpStatus.OK);
    }

    @Operation(summary = "Supprimer un hopital", tags = "hospital", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Object> deleteHospital(@PathVariable Long id) {
        if (!hospitalService.exisbyId(id)) {
            return ResponseEntity.badRequest().body(
                    new MessageResponseDto(HttpStatus.BAD_REQUEST, messageSource.getMessage("messages.id_not_exists", null, LocaleContextHolder.getLocale())));
        }
        Hospitals hospitals = hospitalService.deleteHospital(id);
        if (hospitals.isDelete()) {
            return ResponseEntity.ok(new MessageResponseDto(HttpStatus.OK,  messageSource.getMessage("messages.delete", null, LocaleContextHolder.getLocale())));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDto(HttpStatus.OK, messageSource.getMessage("messages.not_delete", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Supprimer définitivement un hopital", tags = "hospital", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id:[0-9]+}/delete")
    public ResponseEntity<Object> deleteDefinitiveHospital(@PathVariable Long id) {
        checkIfHospitalExist(id);
         hospitalService.deleteDefinitiveHospital(id);
        return ResponseEntity.ok(new MessageResponseDto(HttpStatus.OK,  messageSource.getMessage("messages.delete", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Restorer un hopital", tags = "hospital", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id:[0-9]+}/restore")
    public ResponseEntity<Object> restoreHospital(@PathVariable Long id) {
        checkIfHospitalExist( id);
         hospitalService.restoreHospital(id);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponseDto(HttpStatus.OK, messageSource.getMessage("messages.retore", null, LocaleContextHolder.getLocale())));
    }

    void checkIfHospitalExist(Long id) {
        if (!hospitalService.exisbyId(id)) {
             ResponseEntity.badRequest().body(new MessageResponseDto(HttpStatus.BAD_REQUEST, messageSource.getMessage("messages.id_not_exists", null, LocaleContextHolder.getLocale())));
        }

    }
}
