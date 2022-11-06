package com.sesa.medical.adwapay;


import com.sesa.medical.adwapay.dto.*;
import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.packsesa.entities.PackSesa;
import com.sesa.medical.packsesa.service.IPackSesaService;
import com.sesa.medical.patient.dto.LocationDto;
import com.sesa.medical.patient.entities.*;
import com.sesa.medical.patient.repository.IAbonnementRepo;
import com.sesa.medical.patient.repository.IPayementRepo;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.security.jwt.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.*;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@RestController
@Tag(name = "payement")
@RequestMapping("/api/v1.0/paiement")
@Slf4j
public class AdwaPayRestController {

    @Value("${Merchant-key}")
    private String marchant_key;

    @Value("${Subscription-key}")
    private String subscription_key;

    @Value("${Application-code}")
    private String application_code;

    @Value("${APIs-base-link}")
    private String apis_base_link;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    IPackSesaService packSesaService;

    @Autowired
    IPatientService patientService;

    @Autowired
    IAbonnementRepo abonnementRepo;

    @Autowired
    IPayementRepo payementRepo;

    @Operation(summary = "génération du token pour effectuer un paiement sur l'api de ADWA Pay", tags = "payement", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN')")
    @GetMapping("/adwapay_token_generate")
    public ResponseEntity<String> createTokenApi() {
        final String s = marchant_key + ":" + subscription_key;
        final byte[] authBytes = s.getBytes(StandardCharsets.UTF_8);
        final String encoded = Base64.getEncoder().encodeToString(authBytes);
        String responseObject = "";
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(2, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(2, TimeUnit.MINUTES) // write timeout
                .readTimeout(2, TimeUnit.MINUTES); // read timeout
        OkHttpClient client = builder.build();
        MediaType mediaType = MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\"application\": \""+application_code+"\"}");
        Request request = new Request.Builder()
                .url(apis_base_link+"/getADPToken")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("Authorization", "Basic " + encoded)
                .build();
        try {

            Response response = client.newCall(request).execute();
            System.out.println("code de la reponse:" + response.code());
            responseObject = response.body().string();
            response.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(responseObject, HttpStatus.OK);
    }

    @Operation(summary = "avoir les détails de commision applicable par ADWA sur le prix d'un pack", tags = "payement", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN')")
    @PostMapping("/getCommissionAmount")
    public ResponseEntity<String> getCommissionAmount(@Valid @RequestBody AddFeesDto model) {
        PackSesa packSesa = packSesaService.getById(model.getPackId());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(2, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(2, TimeUnit.MINUTES) // write timeout
                .readTimeout(2, TimeUnit.MINUTES); // read timeout
        String responseObject = "";
        OkHttpClient client = builder.build();
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("currency", "XAF");
        jsonObj.put("amount", packSesa.getPrice());
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, jsonObj.toString());
        Request request = new Request.Builder()
                .url(apis_base_link + "/getFees")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("AUTH-API-TOKEN", "Bearer " + model.getTokenCode())
                .addHeader("AUTH-API-SUBSCRIPTION", subscription_key)
                .build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println("reponsePayement_orange_money: "+ response.code());
            System.out.println("reponsePayement_orange_money: "+ response.toString());
            responseObject = response.body().string();
            response.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }
    @Operation(summary ="Procéder au paiement d'un pack sur sesa", tags = "payement", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN')")
    @PostMapping("/requestToPay")
    public ResponseEntity<String> requestToPay(@Valid @RequestBody RequestToPayDto model) {
        ModePay modePay = patientService.getOneMode(model.getModePayId());
        Patient patient = patientService.getOnePatient(model.getUserId());
        PackSesa packSesa = packSesaService.getById(model.getPackId());
        Abonnement abonnementFind = null;
        Payement payementfind = null;
        if(patient.getAbonnements().size()!=0) {
            for (Abonnement abo : patient.getAbonnements()) {
                if (abo.isEtat() == false) {
                    abonnementFind = abo;
                }
            }
            if (abonnementFind != null) {
                abonnementFind.setCreatedAt(LocalDate.now());
                abonnementFind.setEtat(false);
                abonnementFind.setEndDate(LocalDateTime.now().plusMonths(packSesa.getDuration()));
                abonnementFind.setStartDate(LocalDateTime.now());
                abonnementFind.setAmount(packSesa.getPrice());
                abonnementRepo.save(abonnementFind);
                payementfind = payementRepo.findByAbonnement(abonnementFind);
                payementfind.setModePay(modePay);
                payementfind.setAmount(packSesa.getPrice());
                payementfind.setIdTransaction(jwtUtils.generateIdTransaction());
                payementfind.setCreatedAt(new Date());
                payementfind.setEtat(false);
                payementfind.setUpdateAt(new Date());
                payementRepo.save(payementfind);
            } else {
                Abonnement abonnement = new Abonnement();
                abonnement.setCreatedAt(LocalDate.now());
                abonnement.setEtat(false);
                abonnement.setEndDate(LocalDateTime.now().plusMonths(packSesa.getDuration()));
                abonnement.setStartDate(LocalDateTime.now());
                abonnement.setAmount(packSesa.getPrice());
                abonnementFind = patientService.createAbonnement(abonnement, patient.getUserId(), 1L, packSesa.getId());
                Payement payement = new Payement();
                payement.setModePay(modePay);
                payement.setAmount(packSesa.getPrice());
                payement.setIdTransaction(jwtUtils.generateIdTransaction());
                payement.setCreatedAt(new Date());
                payement.setEtat(false);
                payement.setUpdateAt(new Date());
                payementfind = patientService.createPayement(payement, abonnementFind.getId(), modePay.getId());
            }

        } else  {
            Abonnement abonnement = new Abonnement();
            abonnement.setCreatedAt(LocalDate.now());
            abonnement.setEtat(false);
            abonnement.setEndDate(LocalDateTime.now().plusMonths(packSesa.getDuration()));
            abonnement.setStartDate(LocalDateTime.now());
            abonnement.setAmount(packSesa.getPrice());
            abonnementFind = patientService.createAbonnement(abonnement, patient.getUserId(), 1L, packSesa.getId());
            Payement payement = new Payement();
            payement.setModePay(modePay);
            payement.setAmount(packSesa.getPrice());
            payement.setIdTransaction(jwtUtils.generateIdTransaction());
            payement.setCreatedAt(new Date());
            payement.setEtat(false);
            payement.setUpdateAt(new Date());
            payementfind = patientService.createPayement(payement, abonnementFind.getId(), modePay.getId());
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(2, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(2, TimeUnit.MINUTES) // write timeout
                .readTimeout(2, TimeUnit.MINUTES); // read timeout
        String responseObject = "";
        OkHttpClient client = builder.build();
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("currency", "XAF");
        jsonObj.put("meanCode", modePay.getName());
        jsonObj.put("paymentNumber", model.getPaymentNumber());
        jsonObj.put("orderNumber", payementfind.getIdTransaction());
        jsonObj.put("amount", packSesa.getPrice());
        jsonObj.put("feesAmount", model.getCommisssionAmount());
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, jsonObj.toString());
        Request request = new Request.Builder()
                .url(apis_base_link + "/requestToPay")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("AUTH-API-TOKEN", "Bearer " + model.getTokenCode())
                .addHeader("AUTH-API-SUBSCRIPTION", subscription_key)
                .build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println("reponsePayement_orange_money: "+ response.code());
            System.out.println("reponsePayement_orange_money: "+ response.toString());
            responseObject = response.body().string();
            response.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }
    @Operation(summary ="Vérifier le status d'un paiement jusqu'a son expiration (en cas de succès de paiement le status de l'abonnment du patient est mis à jour)", tags = "payement", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN')")
    @PostMapping("/paymentStatus")
    public ResponseEntity<String> getStatusOfPayement(@Valid @RequestBody StatusPayDto model) {
        ModePay modePay = patientService.getOneMode(model.getModepayId());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(2, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(2, TimeUnit.MINUTES) // write timeout
                .readTimeout(2, TimeUnit.MINUTES); // read timeout
        String responseObject = "";
        OkHttpClient client = builder.build();
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("meanCode", modePay.getName());
        jsonObj.put("adpFootprint", model.getAdpFootprint());
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, jsonObj.toString());
        Request request = new Request.Builder()
                .url(apis_base_link + "/paymentStatus")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("AUTH-API-TOKEN", "Bearer " + model.getTokenCode())
                .addHeader("AUTH-API-SUBSCRIPTION", subscription_key)
                .build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println("reponsePayement_orange_money: "+ response.code());
            System.out.println("reponsePayement_orange_money: "+ response.toString());
            responseObject = response.body().string();
            JSONParser parser = new JSONParser();
           // org.json.simple.JSONObject resp = (org.json.simple.JSONObject) parser.parse(responseObject);
            org.json.JSONObject   resp = new org.json.JSONObject(responseObject);
            log.info("response request:"+resp.toString());
            org.json.JSONObject jsonmessage = (org.json.JSONObject) resp.get("data");
            String status = jsonmessage.get("status").toString();
            String transactionId = jsonmessage.get("orderNumber").toString();
            if(status.equals("T")) {
                Payement pay=    patientService.updatePayementAndAbonnement(transactionId);
                patientService.changeStatusAccountToMember(pay.getAbonnement().getPatient().getUserId(), EStatusAccount.MEMBER);
            }
            response.close();
        } catch (IOException  ex) {
            ex.printStackTrace();
        }



        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }
    @Operation(summary ="relancer le popup dialog USSD sur le mobile afin l'utilisateur confirme la transaction", tags = "payement", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PostMapping("/pushDialog")
    public ResponseEntity<String> getPushDialog(@Valid @RequestBody PushDialogDto model) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(2, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(2, TimeUnit.MINUTES) // write timeout
                .readTimeout(2, TimeUnit.MINUTES); // read timeout
        String responseObject = "";
        OkHttpClient client = builder.build();
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("adpFootprint", model.getAdpFootprint());
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, jsonObj.toString());
        Request request = new Request.Builder()
                .url(apis_base_link + "/pushDialog")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("AUTH-API-TOKEN", "Bearer " + model.getTokenCode())
                .addHeader("AUTH-API-SUBSCRIPTION", subscription_key)
                .build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println("reponsePayement_orange_money: "+ response.code());
            System.out.println("reponsePayement_orange_money: "+ response.toString());
            responseObject = response.body().string();
            response.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }





    }
