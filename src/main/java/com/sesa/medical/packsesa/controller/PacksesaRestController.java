package com.sesa.medical.packsesa.controller;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.packsesa.dto.CategorieDto;
import com.sesa.medical.packsesa.dto.PackSesaDto;
import com.sesa.medical.packsesa.entities.Categorie;
import com.sesa.medical.packsesa.entities.PackSesa;
import com.sesa.medical.packsesa.service.IPackSesaService;
import com.sesa.medical.patient.dto.ParameterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/packsesa")
@Tag(name = "Pack SeSa")
@Slf4j
public class PacksesaRestController {

    @Autowired
    IPackSesaService packSesaService;


    @Operation(summary = "creation d'un catégorie de pack", tags = "Pack SeSa", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Categorie.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @RequestMapping(method=RequestMethod.POST,value="/categorie",produces = "application/json;charset=UTF-8")
    //@PostMapping("/categorie")
    public ResponseEntity<?> createCategorie(@RequestBody List<CategorieDto> categorieDto) {
     List<Categorie> list = new ArrayList<>();
        categorieDto.forEach(cat -> {
            Categorie categorie = new Categorie();
            categorie.setNom(cat.getNom());
            categorie.setDescription(cat.getDescription());
            Categorie cate = packSesaService.create(categorie);
            list.add(cate);
     });
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "creation des paramêtres d'un patient", tags = "Pack SeSa", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = PackSesa.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping()
    public ResponseEntity<?> createPackSesa(@RequestBody List<PackSesaDto> packSesaDtos) {
        List<PackSesa> list = new ArrayList<>();
        packSesaDtos.forEach(pack -> {
            PackSesa packSesa = new PackSesa();
            packSesa.setAcronyme(pack.getAcronyme());
            packSesa.setDescription(pack.getDescription());
            packSesa.setDuration(pack.getDuration());
            packSesa.setPrice(pack.getPrice());
            PackSesa save = packSesaService.createPack(packSesa,pack.getCategorieId());
            list.add(save);
        });
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Liste des catégorie de pack pour SeSa", tags = "Pack SeSa", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Categorie.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @RequestMapping(method=RequestMethod.GET,value="/categorie",produces = "application/json;charset=UTF-8")
    //@GetMapping("/categorie")
    public ResponseEntity<?>  getListCategorie() {
     List<Categorie> list = packSesaService.getAllCategorie();
     return ResponseEntity.ok(list);
    }

    @Operation(summary = "Liste des pack pour SeSa en foction de la catégorie", tags = "Pack SeSa", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = PackSesa.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @RequestMapping(method=RequestMethod.GET,value="/{id}",produces = "application/json;charset=UTF-8")
    //@GetMapping("/{id}")
    public ResponseEntity<?>  getAllPackSeSa(@PathVariable Long id) {
        List<PackSesa> list = packSesaService.getAllPackByCategorie(id);
        return ResponseEntity.ok(list);
    }



}
