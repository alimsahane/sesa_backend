package com.sesa.medical.users.services.imp;

import com.sesa.medical.adwapay.dto.PrestapayDtoJms;
import com.sesa.medical.patient.dto.SosDtoJms;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.security.dto.OtpCodeDto;
import com.sesa.medical.sos.entities.Sos;
import com.sesa.medical.users.dto.SendSmSDto;
import com.sinch.xms.ApiConnection;
import com.sinch.xms.ApiException;
import com.sinch.xms.SinchSMSApi;
import com.sinch.xms.api.MtBatchTextSmsResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SendingSmsService {

    @Value("${app.cmtelecom.api_token}")
    public String app_cmtelecom_api_token;

    @Value("${app.sinch.service_plan_id}")
    public String app_sinch_plan_id;
    @Value("${app.sinch.api_token}")
    public String app_sinch_api_token;

    @Value("${app.mboadeals.userId}")
    public String app_mboadeals_userId;
    @Value("${app.mboadeals.password}")
    public String app_mboadeals_password;


    @Autowired
    IPatientService patientService;

    public void sendsmspersoToPrestaCmtelecom(SosDtoJms sosDtoJms) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES); // read timeout
        JSONObject messages = new JSONObject();
        JSONObject jsonfinal = new JSONObject();
        JSONObject authentication = new JSONObject();
        JSONObject bodymessage = new JSONObject();
        JSONObject body2 = new JSONObject();
        JSONObject toObject = new JSONObject();
        ArrayList<JSONObject> array = new ArrayList<>();
        ArrayList<JSONObject> to = new ArrayList<>();
        ArrayList<String> chanel = new ArrayList<>();
        chanel.add("SMS");
        toObject.put("number", sosDtoJms.getReceiver().getToPhone());
        to.add(toObject);
        bodymessage.put("from", "SOS SESA");
        body2.put("content", sosDtoJms.getMessage());
        body2.put("type", "auto");
        bodymessage.put("body", body2);
        bodymessage.put("to", to);
        bodymessage.put("alowedChannels", chanel);
        array.add(bodymessage);
        authentication.put("productToken", app_cmtelecom_api_token);
        jsonfinal.put("authentication", authentication);
        jsonfinal.put("msg", array);
        messages.put("messages", jsonfinal);
        OkHttpClient client = builder.build();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, messages.toString());
        Request request = new Request.Builder()
                .url("https://gw.cmtelecom.com/v1.0/message")
                .post(body)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .build();
        Response response = null;
        JSONObject resp = null;
        org.json.JSONArray respMessage = null;
        try {
            response = client.newCall(request).execute();
            resp = new JSONObject(response.body().string());
            respMessage = resp.getJSONArray("messages");
        } catch (Exception e) {
            e.printStackTrace();
        }
        int errorCode = resp.getInt("errorCode");
        JSONObject jsonmessage = (JSONObject) respMessage.get(0);
        String parts = jsonmessage.get("parts").toString();
        String status = jsonmessage.get("status").toString();
        String messageErrorCode = jsonmessage.get("messageErrorCode").toString();
        System.out.println("errorcode: " + errorCode + "" + " status: " + status + " parts: " + parts);

       /*
        if(errorCode==0 || errorCode==1) {
            smsDtoPrestaTansition.setStatusEnvoi(true);
            smsDtoPrestaTansition.setStatus("SENT");

        }else {
            smsDtoPrestaTansition.setStatusEnvoi(false);
            smsDtoPrestaTansition.setStatus("FAILED");
        }

        if (jsonmessage != null ) {
            smsDtoPrestaTansition.setNumSegments(parts);
            smsDtoPrestaTansition.setUiid(status);
            smsDtoPrestaTansition.setErrorMessage(messageErrorCode);
        }
*/
    }

    public void sendsmspersoToPrestaMboadeal(String senderName, String receiverNumber,String message) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES); // read timeout
        SendSmSDto demo = new SendSmSDto();
        demo.setSenderid(senderName);
        demo.setMobiles(receiverNumber);
        demo.setSms(message);
        demo.setUser(app_mboadeals_userId);
        demo.setPassword(app_mboadeals_password);
        JSONObject messages = new JSONObject(demo);
        log.info("chaine json du message: "+ messages.toString());
        /*String data = "{\n \"user_id\":\""+app_mboadeals_userId+"\",\n \"message\":\""+message+"\",\n    \"password\":\""+app_mboadeals_password+"\",\n    \"phone_str\":\""+receiverNumber+"\",\n    \"sender_name\":\""+senderName+"\"\n}";
        JSONObject messages = new JSONObject(data);*/
        OkHttpClient client = builder.build();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, messages.toString());
        Request request = new Request.Builder()
                .url("https://smsvas.com/bulk/public/index.php/api/v1/sendsms")
                .post(body)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .build();
        Response response = null;
        org.json.JSONArray respMessage = null;
        try {
            response = client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendSmsPersoPrestaSinch(SosDtoJms sosDtoJms) throws IOException {
        log.info("object: " + sosDtoJms.toString());
        MtBatchTextSmsResult result = null;
        ApiConnection conn = ApiConnection.builder()
                .servicePlanId(app_sinch_plan_id)
                .token(app_sinch_api_token)
                .start();
        try {
            result =
                    conn.createBatch(SinchSMSApi.batchTextSms()
                            .sender("SOS SeSa")
                            .addRecipient(sosDtoJms.getReceiver().getToPhone())
                            .body(sosDtoJms.getMessage())
                            .callbackUrl(URI.create("https://run.mocky.io/v3/12cbf985-3167-4d92-9765-184a040bcbd2"))
                            .build());

            log.info("SMS sent with batch ID " + result.id());

        } catch (InterruptedException e) {

            log.error(
                    "ERROR", "CODE APP : SESA" + " | " + e + " A LA DATE: " + new Date());
        } catch (ApiException e) {
            log.error("SMS could not be sent.", e);
        } finally {
            conn.close();
        }
        Sos sos = new Sos();
        if (result.canceled()) {
            sos.setStatusEnvois(false);
            sos.setEtat(false);
        } else {
            sos.setStatusEnvois(true);
            sos.setEtat(true);
        }
        sos.setProviderName("sms");
        sos.setMessage(sosDtoJms.getMessage() + "poids: " + sosDtoJms.getPoids() + " taille: " + sosDtoJms.getTaille() + "température: " + sosDtoJms.getTemperature() + " poul: " + sosDtoJms.getPouls());
        patientService.CreateSosPatient(sos, sosDtoJms.getSender().getId(), sosDtoJms.getReceiver().getId());
    }

    public void sendSmsOtpCodePersoPrestaSinch(OtpCodeDto otpCodeDto) throws IOException {
        log.info("object: " + otpCodeDto.toString());
        MtBatchTextSmsResult result = null;
        ApiConnection conn = ApiConnection.builder()
                .servicePlanId(app_sinch_plan_id)
                .token(app_sinch_api_token)
                .start();
        try {
            result =
                    conn.createBatch(SinchSMSApi.batchTextSms()
                            .sender("SeSa Auth")
                            .addRecipient(otpCodeDto.getTel())
                            .body("Votre code OTP est : " + URLEncoder.encode(otpCodeDto.getCode(), String.valueOf(StandardCharsets.UTF_8)))
                            .callbackUrl(URI.create("https://run.mocky.io/v3/12cbf985-3167-4d92-9765-184a040bcbd2"))
                            .build());

            log.info("SMS otp code sent with batch ID " + result.id());

        } catch (InterruptedException e) {

            log.error(
                    "ERROR", "CODE APP : SESA" + " | " + e + " A LA DATE: " + new Date());
        } catch (ApiException e) {
            log.error("SMS could not be sent.", e);
        } finally {
            conn.close();
        }
    }

    public void sendSmsAfterPayPresta(PrestapayDtoJms jms) throws IOException {
        log.info("object: " + jms.toString());
        MtBatchTextSmsResult result = null;
        ApiConnection conn = ApiConnection.builder()
                .servicePlanId(app_sinch_plan_id)
                .token(app_sinch_api_token)
                .start();
        try {
            result =
                    conn.createBatch(SinchSMSApi.batchTextSms()
                            .sender("Presta Pay")
                            .addRecipient(jms.getReceiverNumber())
                            .body("Paiement d'une prestation avec succès : " + "\n"
                                    + "Nom :" + jms.getCompletName() + "\n"
                                    + "orderNumber: " + jms.getPhone() + "\n"
                                    + "amount: " + jms.getAmount() + "\n"
                                    + "mode pay: " + jms.getModePay()+ "\n"
                                    + "Date pay" + jms.getDate()+ "\n"
                                    + "presta description: " + jms.getDescription())
                            .callbackUrl(URI.create("https://run.mocky.io/v3/12cbf985-3167-4d92-9765-184a040bcbd2"))
                            .build());

            log.info("SMS otp code sent with batch ID " + result.id());

        } catch (InterruptedException e) {

            log.error(
                    "ERROR", "CODE APP : SESA" + " | " + e + " A LA DATE: " + new Date());
        } catch (ApiException e) {
            log.error("SMS could not be sent.", e);
        } finally {
            conn.close();
        }
    }
}
