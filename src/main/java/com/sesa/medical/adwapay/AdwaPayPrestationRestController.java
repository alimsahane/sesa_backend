package com.sesa.medical.adwapay;

import com.sesa.medical.adwapay.dto.*;
import com.sesa.medical.globalconfig.ApplicationConstant;
import com.sesa.medical.packsesa.entities.PackSesa;
import com.sesa.medical.patient.entities.EStatusAccount;
import com.sesa.medical.patient.entities.ModePay;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.patient.entities.Payement;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.prestation.entities.PaiementPresta;
import com.sesa.medical.prestation.entities.Prestation;
import com.sesa.medical.prestation.repositoty.PaiementPrestaRepo;
import com.sesa.medical.prestation.repositoty.PrestationRepo;
import com.sesa.medical.prestation.service.PrestationService;
import com.sesa.medical.security.dto.MessageResponseDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@Tag(name = "prestation")
@RequestMapping("/api/v1.0/prestation")
@Slf4j
public class AdwaPayPrestationRestController {
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
    IPatientService patientService;

    @Autowired
    PrestationService prestationService;

    @Autowired
    PaiementPrestaRepo paiementPrestaRepo;

    @Autowired
    PrestationRepo prestationRepo;

    @Autowired
    JmsTemplate jmsTemplate;

    @Operation(summary = "Recupérer la liste des prestations d'un patient", tags = "Patients", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Patient.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN,ROLE_MEDECIN') AND @authorizationService.canUpdateOwnerItem(#id,'User')")
   // @GetMapping("/{id}")
    @RequestMapping(method=RequestMethod.GET,value="/{id}",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?>  getOnePatient(@PathVariable Long id) {
        List<Prestation> prestationList = prestationService.getListPrestationPatient(id);
        return new ResponseEntity<>(prestationList,HttpStatus.OK);
    }

    @Operation(summary = "génération du token pour effectuer un paiement sur l'api de ADWA Pay", tags = "payement", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN')")
    @GetMapping("/paiement/adwapay_token_generate")
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
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\"application\": \"" + application_code + "\"}");
        Request request = new Request.Builder()
                .url(apis_base_link + "/getADPToken")
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
    @PostMapping("/paiement/getCommissionAmount")
    public ResponseEntity<?> getCommissionAmount(@Valid @RequestBody AddFeePrestaDto model) {
        if (model.getAmount() < 500) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(HttpStatus.BAD_REQUEST, "le montant de la prestation doit être superieur ou égal à 500F"));
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
        jsonObj.put("amount", model.getAmount());
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
            System.out.println("reponsePayement_orange_money: " + response.code());
            System.out.println("reponsePayement_orange_money: " + response.toString());
            responseObject = response.body().string();
            response.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }

    @Operation(summary = "Procéder au paiement d'une prestation", tags = "payement", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN')")
    @PostMapping("/paiement/requestToPay")
    public ResponseEntity<?> requestToPay(@Valid @RequestBody RequestToPayPrestaDto model) {
        if (model.getAmount() < 500) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(HttpStatus.BAD_REQUEST, "le montant de la prestation doit être superieur ou égal à 500F"));
        }
        ModePay modePay = patientService.getOneMode(model.getModePayId());
        Patient patient = patientService.getOnePatient(model.getUserId());
        Prestation presta = new Prestation();
        presta.setStatusPay(false);
        presta.setCreatedAt(LocalDateTime.now());
        presta.setDescription(model.getDescriptionPresta());
        presta.setMontant(String.valueOf(model.getAmount()));
        presta.setPayNumber(model.getPaymentNumber());
        Prestation prestaSave = prestationService.createPrestation(presta, patient.getUserId());
        PaiementPresta paie = new PaiementPresta();
        paie.setStatusPay(false);
        paie.setMontant(String.valueOf(model.getAmount()));
        paie.setPayNumber(model.getPaymentNumber());
        paie.setIdTransaction(jwtUtils.generateIdTransaction());
        paie.setCreatedAt(LocalDateTime.now());
        PaiementPresta paySave = prestationService.createPaypresta(paie, prestaSave.getId(), modePay.getId());
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
        jsonObj.put("orderNumber", paySave.getIdTransaction());
        jsonObj.put("amount", model.getAmount());
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
            System.out.println("reponsePayement_orange_money: " + response.code());
            System.out.println("reponsePayement_orange_money: " + response.toString());
            responseObject = response.body().string();
            response.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }

    @Operation(summary = "Vérifier le status d'un paiement jusqu'a son expiration (en cas de succès de paiement le status de l'abonnment du patient est mis à jour)", tags = "payement", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_MEDECIN,ROLE_ADMIN')")
    @PostMapping("/paiement/paymentStatus")
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
            System.out.println("reponsePayement_orange_money: " + response.code());
            System.out.println("reponsePayement_orange_money: " + response.toString());
            responseObject = response.body().string();
            JSONParser parser = new JSONParser();
            // org.json.simple.JSONObject resp = (org.json.simple.JSONObject) parser.parse(responseObject);
            org.json.JSONObject resp = new org.json.JSONObject(responseObject);
            log.info("response request:" + resp.toString());
            org.json.JSONObject jsonmessage = (org.json.JSONObject) resp.get("data");
            String status = jsonmessage.get("status").toString();
            String transactionId = jsonmessage.get("orderNumber").toString();
            if (status.equals("T")) {
                PaiementPresta pay = paiementPrestaRepo.findByIdTransaction(transactionId);
                Prestation prestation = prestationRepo.findByPaiementPresta(pay);
                prestationService.updateStatusPrestation(prestation.getId(), true);
                prestationService.updateStatus(pay.getId(), true);
                PrestapayDtoJms jms = new PrestapayDtoJms();
                jms.setAmount(pay.getMontant());
                jms.setDate(pay.getCreatedAt().toString());
                jms.setPhone(pay.getPayNumber());
                jms.setDescription(prestation.getDescription());
                jms.setModePay(pay.getModePay().getName());
                jms.setUserId(prestation.getPatient().getUserId());
                jms.setCompletName(prestation.getPatient().getFirstName() + " " + prestation.getPatient().getLastName());
                jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_SMS_PAYPRESTA_SUCCESSFULL, jms);
            }
            response.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }

    @Operation(summary = "relancer le popup dialog USSD sur le mobile afin l'utilisateur confirme la transaction", tags = "payement", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    @PostMapping("/paiement/pushDialog")
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
            System.out.println("reponsePayement_orange_money: " + response.code());
            System.out.println("reponsePayement_orange_money: " + response.toString());
            responseObject = response.body().string();
            response.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }
}
