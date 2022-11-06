package com.sesa.medical.pharmacie.controller;

import com.sesa.medical.pharmacie.dto.Addmedoc;
import com.sesa.medical.pharmacie.entities.Medicaments;
import com.sesa.medical.pharmacie.service.IMedicamentService;
import com.sesa.medical.users.dto.UploadFileResponseDto;
import com.sesa.medical.users.entities.Users;
import com.sesa.medical.utilities.entities.SliderDocument;
import com.sesa.medical.utilities.service.SliderDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/medicament")
@Tag(name = "Medicaments")
@Slf4j
public class PharmacieRestController {
    @Value("${app.api.base.url}")
    private String api_base_url;

    @Autowired
    IMedicamentService medicamentService;

    @Autowired
    SliderDocumentService sliderDocumentService;


    @Operation(summary = "creation d'un médicament pour une pharmacie", tags = "Medicaments", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = UploadFileResponseDto.class)))),
            @ApiResponse(responseCode = "500", description = "Sorry! Filename contains invalid path sequence / Could not store file", content = @Content(mediaType = "Application/Json"))})
    @PostMapping("")
    public UploadFileResponseDto uploadFile(@RequestParam(required = true) MultipartFile file,@RequestParam(required = true) String description,@RequestParam(required = true)  double amount,@RequestParam(required = false) int quantite,@RequestParam(required = true) boolean status) {
        String fileName = sliderDocumentService.storeFile(file, removeExtension(file.getOriginalFilename()));
        String fileDownloadUri = api_base_url+"/api/v1.0/medicament/downloadFile/"+file.getOriginalFilename();
        Medicaments medicaments = new Medicaments();
        medicaments.setImageUrl(fileDownloadUri);
        medicaments.setAmount(amount);
        medicaments.setDecription(description);
        medicaments.setQuantite(quantite);
        medicaments.setEtat(status);
        medicamentService.addmedicament(medicaments);
        return new UploadFileResponseDto(fileName, fileDownloadUri, file.getContentType(), file.getSize());

    }

    public static String removeExtension(String fileName) {
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }

    }

    @Operation(summary = "récuperer l'image d'un médicament", tags = "Medicaments", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content(mediaType = "Application/Json")), })
    @GetMapping(value = "/downloadFile/{filename}")
    public ResponseEntity<?> downloadFile(@PathVariable String filename,
                                          HttpServletRequest request) throws IOException {
        Resource resource = null;
        if (filename != null && !filename.isEmpty()) {
            try {
                resource = sliderDocumentService.loadFileAsResource(filename);
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
                    .body(IOUtils.toByteArray(resource.getInputStream()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
    }

    @Operation(summary = "Supprimer un médicament", tags = "Medicaments", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "Application/Json")) })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteMedoc(@PathVariable Long id) {
        medicamentService.deleteMedoc(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Liste des médicaments disponible en pharmacie", tags = "Medicaments", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json",array = @ArraySchema(schema = @Schema(implementation = Medicaments.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    //@GetMapping("")
    @RequestMapping(method=RequestMethod.GET,value="",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getAllMedoc() {
        return new ResponseEntity<>(medicamentService.getAllMedoc(), HttpStatus.OK);
    }

    @Operation(summary = "Modifier le status d'un médicaments", tags = "Medicaments", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json",array = @ArraySchema(schema = @Schema(implementation = Medicaments.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
    @GetMapping("/{id}/status/{status}")
    public ResponseEntity<?> updateStatusMedoc(@PathVariable long id,@PathVariable boolean status) {
        return new ResponseEntity<>(medicamentService.updateStatus(status,id), HttpStatus.OK);
    }

    @Operation(summary = "modification d'un lien de video youtube", tags = "Medicaments", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json",array = @ArraySchema(schema = @Schema(implementation = Medicaments.class)))),
            @ApiResponse(responseCode = "404", description = "L'utilisateur n'existe pas dans la BD", content = @Content(mediaType = "Application/Json"))})

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateYoutubeVideo(@RequestBody Addmedoc addmedoc, @PathVariable Long id) {
      Medicaments medicaments = new Medicaments();
      medicaments.setDecription(addmedoc.getDescription());
      medicaments.setAmount(addmedoc.getAmount());
      medicaments.setQuantite(addmedoc.getQuantite());
      Medicaments medicamentsave = medicamentService.updatePharmacie(medicaments,id);
        return ResponseEntity.ok().body(medicamentsave);
    }
}
