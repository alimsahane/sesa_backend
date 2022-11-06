package com.sesa.medical.utilities.controller;

import com.google.common.io.ByteStreams;
import com.sesa.medical.patient.entities.ModePay;
import com.sesa.medical.patient.repository.IModeRepo;
import com.sesa.medical.prestation.entities.DescriptionPrestation;
import com.sesa.medical.prestation.service.PrestationService;
import com.sesa.medical.security.dto.MessageResponseDto;
import com.sesa.medical.security.dto.UserResetPassword;
import com.sesa.medical.users.dto.UploadFileResponseDto;
import com.sesa.medical.users.entities.DocumentStorageProperties;
import com.sesa.medical.users.entities.Users;
import com.sesa.medical.utilities.dto.DescriptionPrestaDto;
import com.sesa.medical.utilities.dto.YoutubeDto;
import com.sesa.medical.utilities.entities.GeneralCondition;
import com.sesa.medical.utilities.entities.PrivacyPolicy;
import com.sesa.medical.utilities.entities.SliderDocument;
import com.sesa.medical.utilities.entities.YoutubeVideo;
import com.sesa.medical.utilities.service.SliderDocumentService;
import com.sesa.medical.utilities.service.UtilitieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@Tag(name = "Utilities")
@RequestMapping("/api/v1.0/utilities")
@Slf4j
public class UtilitiesRest {

    @Autowired
    UtilitieService utilitieService;

    @Autowired
    IModeRepo modeRepo;

    @Autowired
    SliderDocumentService sliderDocumentService;

    @Autowired
    PrestationService prestationService;

    @Value("${app.api.base.url}")
    private String api_base_url;
    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Operation(summary = "Politique de confidentialité de sesa", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @GetMapping("/privacyPolicy")
    public ResponseEntity<PrivacyPolicy> getprivacyPolicy() {
        return new ResponseEntity<>(utilitieService.getprivacyPolicy(), HttpStatus.OK);
    }

    @Operation(summary = "Condition général d'utilisation", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @GetMapping("/generalCondition")
    public ResponseEntity<GeneralCondition> getGeneralCondition() {
        return new ResponseEntity<>(utilitieService.getGeneralCondition(), HttpStatus.OK);
    }

    @Operation(summary = "Liste des photos pour le slider de sesa", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @GetMapping("/sliderFile")
    public ResponseEntity<List<SliderDocument>> getAllSlider() {
        return new ResponseEntity<>(utilitieService.getAllSlider(), HttpStatus.OK);
    }

    @Operation(summary = "Liste des liens de video youtube pour premier secours", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @GetMapping("/youtubelink")
    public ResponseEntity<List<YoutubeVideo>> getAllYoutubeVideo() {
        return new ResponseEntity<>(utilitieService.getAllVideoYoutube(), HttpStatus.OK);
    }

    @Operation(summary = "Liste des modes de paiement disponible", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @GetMapping("/modepay")
    public ResponseEntity<List<ModePay>> getAllModePay() {
        return new ResponseEntity<>(modeRepo.findAll(), HttpStatus.OK);
    }

    @Operation(summary = "enregistrer un lien de video youtube", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = UploadFileResponseDto.class)))),
            @ApiResponse(responseCode = "500", description = "Sorry! Filename contains invalid path sequence / Could not store file", content = @Content(mediaType = "Application/Json"))})
    @PostMapping("/youtubelink")
    public ResponseEntity<YoutubeVideo> saveYoutubeVideo(@RequestBody YoutubeDto youtubeDto) {
        YoutubeVideo video = new YoutubeVideo();
        video.setUrl(youtubeDto.getUrl());
        video.setDescription(youtubeDto.getDescription());
        video.setStatus(youtubeDto.isStatus());
        YoutubeVideo y = utilitieService.createVideo(video);
        return ResponseEntity.ok().body(y);
    }


    @Operation(summary = "Uploader une image pour le slider de sesa ", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = UploadFileResponseDto.class)))),
            @ApiResponse(responseCode = "500", description = "Sorry! Filename contains invalid path sequence / Could not store file", content = @Content(mediaType = "Application/Json"))})
    @PostMapping("/uploadFile/{docType}")
    public UploadFileResponseDto uploadFile(@RequestBody MultipartFile file, @PathVariable("docType") String docType) {
        String fileName = sliderDocumentService.storeFile(file, docType);
        String fileDownloadUri = api_base_url + "/api/v1.0/utilities/downloadFile/" + docType;
        SliderDocument newDoc = new SliderDocument();
        newDoc.setDocumentFormat(file.getContentType());
        newDoc.setFileName(fileName);
        newDoc.setDocumentType(docType);
        newDoc.setUploadDir(fileDownloadUri);
        utilitieService.createSlider(newDoc);
        return new UploadFileResponseDto(fileName, fileDownloadUri, file.getContentType(), file.getSize());

    }

    @Operation(summary = "modification d'un lien de video youtube", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "L'utilisateur n'existe pas dans la BD", content = @Content(mediaType = "Application/Json"))})

    @PutMapping("/youtubelink/{id}")
    public ResponseEntity<Object> updateYoutubeVideo(@RequestBody YoutubeDto youtubeDto, @PathVariable Long id) {
        YoutubeVideo video = new YoutubeVideo();
        video.setUrl(youtubeDto.getUrl());
        video.setDescription(youtubeDto.getDescription());
        video.setStatus(youtubeDto.isStatus());
        YoutubeVideo y = utilitieService.updateVideo(video, id);
        return ResponseEntity.ok().body(y);
    }

    @Operation(summary = "Supprimer un lien de video youtube", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "Application/Json"))})
    @DeleteMapping("/youtubelink/{id}")
    public ResponseEntity<Object> deleteVideo(@PathVariable Long id) {
        utilitieService.deleteVideo(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Supprimer un slider pour sesa", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "Application/Json"))})
    @DeleteMapping("/sliderFile/{id}")
    public ResponseEntity<Object> deleteSliderVideo(@PathVariable Long id) {
        utilitieService.deleteSliderDocument(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }


    @Operation(summary = "Télécharger un document", tags = "users", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content(mediaType = "Application/Json")),})
    @GetMapping(value = "/downloadFile/{docType}")
    public ResponseEntity<?> downloadFile(@PathVariable String docType,
                                          HttpServletRequest request) throws IOException {
        String fileName = sliderDocumentService.getDocumentName(docType);
        Resource resource = null;
        if (fileName != null && !fileName.isEmpty()) {
            try {
                resource = sliderDocumentService.loadFileAsResource(fileName);
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
            byte[] bytes = Files.readAllBytes(Paths.get(resource.getFile().getAbsolutePath()));

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(new ByteArrayResource(bytes));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
    }

    @Operation(summary = "enregistrer une description pour une prestation", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = UploadFileResponseDto.class)))),})
    @PostMapping("/description")
    public ResponseEntity<?> saveDescriptionPresta(@RequestBody DescriptionPrestaDto desc) {
        DescriptionPrestation descriptionPrestation = new DescriptionPrestation();
        descriptionPrestation.setDescription(desc.getDescription());
        return new ResponseEntity<>(prestationService.createDescription(descriptionPrestation), HttpStatus.CREATED);
    }

    @Operation(summary = "Liste des descriptions de prestation pour l'auto completion", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = UploadFileResponseDto.class)))),})
    @RequestMapping(method=RequestMethod.GET,value="/description",produces = "application/json;charset=UTF-8")
   // @GetMapping("/description")
    public ResponseEntity<?> getAllDescriptionPresta() {
        return ResponseEntity.ok(prestationService.getAllDescriptionPresta());
    }

    @Operation(summary = "supprimer une description pour une prestation", tags = "Utilities", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = UploadFileResponseDto.class)))),})
    @GetMapping("/description/{id}")
    public ResponseEntity<?> getAllDescriptionPresta(@PathVariable Long id) {
        prestationService.deleteDescription(id);
        return ResponseEntity.ok(new MessageResponseDto(
                messageSource.getMessage("messages.parameter-successful-delete", null, LocaleContextHolder.getLocale())));
    }

}
