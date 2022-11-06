package com.sesa.medical.patient.controller;

import com.sesa.medical.globalconfig.ApplicationConstant;
import com.sesa.medical.hopital.dto.HospitalDto;
import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.patient.dto.*;
import com.sesa.medical.patient.entities.Abonnement;
import com.sesa.medical.patient.entities.Parametre;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.security.dto.MessageResponseDto;
import com.sesa.medical.users.entities.Users;
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
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/patient")
@Tag(name = "Patients")
@Slf4j
public class PatientRestController {
    @Autowired
    IPatientService patientService;
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Parameters(value = {
            @Parameter(name = "sort", schema = @Schema(allowableValues = {"lastName", "birthdate", "birthdatePlace",
                    "sexe", "maritalStatus", "nationality"})),
            @Parameter(name = "order", schema = @Schema(allowableValues = {"asc", "desc"}))})
    @Operation(summary = "Recupérer la liste paginée des patients", tags = "Patients", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Patient.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasRole('ROLE_MEDECIN') OR hasRole('ROLE_ADMIN')")
    @GetMapping()
    public ResponseEntity<Page<Patient>> getAllPatient(@RequestParam(required = false, value = "page", defaultValue = "0") int pageParam,
                                                       @RequestParam(required = false, value = "size", defaultValue = "20") int sizeParam,
                                                       @RequestParam(required = false, defaultValue = "userId") String sort,
                                                       @RequestParam(required = false, defaultValue = "desc") String order) {
        return new ResponseEntity<>(patientService.getListPaginatePatient(pageParam, sizeParam, sort, order), HttpStatus.OK);
    }
    @Operation(summary = "Recupérer un patient", tags = "Patients", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Patient.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN,ROLE_MEDECIN') AND @authorizationService.canUpdateOwnerItem(#id,'User')")
    @GetMapping("/{id}")
    public ResponseEntity<Patient>  getOnePatient(@PathVariable Long id) {
        Patient patient = patientService.getOnePatient(id);
        return new ResponseEntity<>(patient,HttpStatus.OK);
    }

    @Operation(summary = "Recupérer l'abonnement actif d'un patient", tags = "Patients", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Patient.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @GetMapping("/{id}/abonnement")
    public ResponseEntity<?>  getAbonnementPatient(@PathVariable Long id) {
        Patient patient = patientService.getOnePatient(id);
        Abonnement abonnement = patientService.findAbonnementByPatient(patient);
        return new ResponseEntity<>(abonnement,HttpStatus.OK);
    }

    @Operation(summary = "Recupérer la liste des abonnements d'un patient", tags = "Patients", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Patient.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @GetMapping("/{id}/abonnementlist")
    public ResponseEntity<?>  getListAbonnementPatient(@PathVariable Long id) {
        List<Abonnement> abonnement = patientService.getAllAbonnementPatient(id);
        return new ResponseEntity<>(abonnement,HttpStatus.OK);
    }

    @Operation(summary = "creation d'un SOS", tags = "Patient", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Hospitals.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN') AND @authorizationService.canUpdateOwnerItem(#id,'User')")
    @PostMapping("/{id}/sos")
    public ResponseEntity<?> createHospital(@Valid @RequestBody LocationDto locationDto,@PathVariable Long id) {
          Patient patient = patientService.getOnePatient(id);
          if(patientService.checkInformationOfPatient(patient)) {
              Abonnement abonnement = patientService.findAbonnementByPatient(patient);
              List<Doctors> doctors = patientService.findDoctorByHospitalAndSosreceiveTrue(abonnement.getHospitals());
              for (Doctors doc: doctors) {
                  SenderDto senderDto = new SenderDto();
                  senderDto.setId(patient.getUserId());
                  senderDto.setFromEmail(patient.getEmail());
                  senderDto.setFromName(patient.getFirstName() +"" + patient.getLastName());
                  senderDto.setFromPhone(patient.getTel1());
                  ReceiverDto receiver = new ReceiverDto();
                  receiver.setId(doc.getUserId());
                  receiver.setToEmail(doc.getEmail());
                  receiver.setToName(doc.getFirstName() +""+doc.getLastName());
                  receiver.setToPhone(doc.getTel1());
                  SosDtoJms sosDtoJms = new SosDtoJms();
                  sosDtoJms.setSender(senderDto);
                  sosDtoJms.setReceiver(receiver);
                  Parametre param = patientService.getParametrePatient(patient);
                  sosDtoJms.setTaille(param.getTaille());
                  sosDtoJms.setPoids(param.getPoids());
                  sosDtoJms.setTemperature(param.getTemperature());
                  sosDtoJms.setFrequenceCardiaque(param.getFrequenceCardiaque());
                  sosDtoJms.setFrequenceRespiratoire(param.getFrequenceRespiratoire());
                  sosDtoJms.setPerimetreBranchial(param.getPerimetreBranchial());
                  sosDtoJms.setPouls(param.getPouls());
                  sosDtoJms.setSaturationOxygene(param.getSaturationOxygene());
                  sosDtoJms.setLatitude(locationDto.getLatitude());
                  sosDtoJms.setLongitude(locationDto.getLongitude());
                  sosDtoJms.setName(locationDto.getName());
                  if(locationDto.getMessage().length()>0) {
                      sosDtoJms.setMessage("Nom: "+patient.getFirstName()+" "+ patient.getLastName() +"\n " +"Phone: "+patient.getTel1()+"\n " +locationDto.getMessage()+".\n " +"poids: " +sosDtoJms.getPoids()+" taille: "+sosDtoJms.getTaille()+"température: "+sosDtoJms.getTemperature()+ " poul: "+sosDtoJms.getPouls() +"\n" +"position : "+" https://www.google.com/maps/search/?api=1&query="+locationDto.getLatitude()+","+locationDto.getLongitude());
                  } else {
                      sosDtoJms.setMessage("Nom: "+patient.getFirstName()+" "+ patient.getLastName() +"\n " +"Phone: "+patient.getTel1()+"\n " +patientService.getOneTemplate().getMessage()+".\n "+"poids: " +sosDtoJms.getPoids()+" taille: "+sosDtoJms.getTaille()+"température: "+sosDtoJms.getTemperature()+ " poul: "+sosDtoJms.getPouls()+"\n" +"position actuel :"+" https://www.google.com/maps/search/?api=1&query="+locationDto.getLatitude()+","+locationDto.getLongitude());

                  }
                  jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_EMAIL_SOS,sosDtoJms);
                  jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_SMS_SOS,sosDtoJms);
                  jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_PUSH_SOS,sosDtoJms);
              }
          }

        return new ResponseEntity<>(new MessageResponseDto(HttpStatus.CREATED,"messages.sos.response"),HttpStatus.CREATED);
    }

    @Operation(summary = "creation des paramêtres d'un patient", tags = "Patient", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Hospitals.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN') AND @authorizationService.canUpdateOwnerItem(#id,'User')")
    @PostMapping("/{id}/parameter")
    public ResponseEntity<?> createParameterPatient(@Valid @RequestBody ParameterDto parameterDto, @PathVariable Long id) {
        Parametre parametre = modelMapper.map(parameterDto, Parametre.class);
       Parametre save =   patientService.createParametrePatient(parametre,id);
       return  new ResponseEntity<>(save,HttpStatus.CREATED);
    }

    @Operation(summary = "Modification des paramêtres d'un patient", tags = "Patient", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Hospitals.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN')")
    @PutMapping("/parameter/{id}")
    public ResponseEntity<?> updateParameterPatient(@Valid @RequestBody ParameterDto parameterDto, @PathVariable Long id) {
        Parametre parametre = modelMapper.map(parameterDto, Parametre.class);
        Parametre save =   patientService.updateParametre(parametre,id);
        return  new ResponseEntity<>(save,HttpStatus.CREATED);
    }


    @Operation(summary = "Supprimer un paramêtre pour un patient", tags = "Patient", responses = {
            @ApiResponse(responseCode = "200", description = "Paramêtre deleted successfully", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : access denied", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN')")
    @DeleteMapping("/parameter/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        patientService.deleteParametre(id);
        return ResponseEntity.ok(new MessageResponseDto(
                messageSource.getMessage("messages.parameter-successful-delete", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Liste paginée des paramêtres d'un patient", tags = "Patient", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Doctors.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN') OR @authorizationService.canUpdateOwnerItem(#id,'User')")
    @GetMapping("/{id}/parameter")
    public ResponseEntity<Page<Parametre>> getAllDoctorPaginate(@PathVariable Long id,
            @Parameter(description = "number of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "size of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "sort of the page to be find. Cannot be empty.",
                    required = true) @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order) {
        Page<Parametre> page = patientService.getParametreOfPatient(id,pageNo, pageSize, sortBy,order);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }


}
