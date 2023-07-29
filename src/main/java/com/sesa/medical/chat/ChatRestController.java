/*
package com.sesa.medical.chat;

import com.pusher.rest.Pusher;
import com.sesa.medical.chat.dto.NotificationCountDto;
import com.sesa.medical.medecin.service.IDoctorService;
import com.sesa.medical.patient.dto.ChatDto;
import com.sesa.medical.patient.entities.Chat;
import com.sesa.medical.patient.entities.EStatusMessage;
import com.sesa.medical.patient.entities.EmessageType;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.pharmacie.entities.Medicaments;
import com.sesa.medical.security.dto.MessageResponseDto;
import com.sesa.medical.sos.dto.PushNotificationRequest;
import com.sesa.medical.sos.service.PushNotificationService;
import com.sesa.medical.users.dto.SendMessageDto;
import com.sesa.medical.users.dto.UploadFileResponseDto;
import com.sesa.medical.users.entities.Users;
import com.sesa.medical.users.repository.IUsersRepository;
import com.sesa.medical.users.services.IDocumentStorageService;
import com.sesa.medical.users.services.IUserService;
import com.sesa.medical.utilities.service.SliderDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.util.IOUtils;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Tag(name = "chat")
@RequestMapping("/api/v1.0/user")
@Slf4j
public class ChatRestController {


    private PushNotificationService pushNotificationService;

    public ChatRestController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @Value("${app.pusher.app_id}")
    String pusherId;

    @Value("${app.pusher.key}")
    String pusherKey;

    @Value("${app.pusher.secret}")
    String pusherSecret;

    @Value("${app.pusher.cluster}")

    String pusherCluster;
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

    @Autowired
    SliderDocumentService sliderDocumentService;
    @Operation(summary = "Envoyer un message à un utilisateur", tags = "users", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Chat.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "403", description = "Forbidden : accès refusé", content = @Content(mediaType = "Application/Json")),})
    //@PostMapping("/sendMessage")
    @RequestMapping(method=RequestMethod.POST,value="/sendMessage",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> saveAdresseUser(@Valid @RequestBody SendMessageDto sendMessageDto) {
        Users sender = userService.getById(sendMessageDto.getSenderId());
        Users receiver = userService.getById(sendMessageDto.getReceiverId());
        Chat chat = new Chat();
        chat.setMessage(sendMessageDto.getMessage());
        chat.setCreatedAt(LocalDateTime.now());
        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setMessageType(EmessageType.text);
        chat.setStatusMessage(EStatusMessage.USER_WRITE);
        Chat chatsave = patientService.sendMessage(chat);
        Pusher pusher = new Pusher(pusherId, pusherKey, pusherSecret);
        pusher.setCluster(pusherCluster);
        pusher.setEncrypted(true);
        Map<String, String> mapping = new HashMap<>();
        mapping.put("senderId", chat.getSender().getUserId().toString());
        mapping.put("receiverId", chat.getReceiver().getUserId().toString());
        mapping.put("message", chat.getMessage());
        mapping.put("messageType", chat.getMessageType().name());
        mapping.put("dateEnvois", chat.getCreatedAt().toString());
        pusher.trigger("channel", "chat", mapping);

        Map<String, String> pushData = new HashMap<>();
        PushNotificationRequest request = new PushNotificationRequest();
        request.setTitle("Nouveau message");
        request.setMessage(chat.getMessage());
        //request.setTopic("com.dev.sesa");
        request.setToken(sendMessageDto.getFcmToken());
        pushData.put("title", "Nouveau message");
        pushData.put("message", chat.getMessage());
        pushData.put("messageType",chat.getMessageType().name());
        pushData.put("dateEnvois",chat.getCreatedAt().toString());
        pushData.put("receiverId",chat.getReceiver().getUserId().toString());
        pushData.put("senderId",chat.getSender().getUserId().toString());
      //  pushData.put("image", "https://raw.githubusercontent.com/Firoz-Hasan/SpringBootPushNotification/master/pushnotificationconcept.png");
        pushData.put("timestamp", chat.getCreatedAt().toString());
       // pushData.put("article_data", "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        pushNotificationService.sendPushNotification(pushData,request);
        return new ResponseEntity<>(chatsave, HttpStatus.CREATED);
    }

    @Operation(summary = "Liste paginée des personnes ayant envoyé un message à un utilisateur", tags = "chat", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Users.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
   // @PreAuthorize("@authorizationService.canUpdateOwnerItem(#id,'User')")
   // @GetMapping("/message/{id}")
    @RequestMapping(method=RequestMethod.GET,value="/message/{id}",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getAllDoctorPaginate(@PathVariable Long id,
                                                           @Parameter(description = "number of the page to be find. Cannot be empty.",
                                                                   required = true) @RequestParam(defaultValue = "0") int pageNo,
                                                           @Parameter(description = "size of the page to be find. Cannot be empty.",
                                                                   required = true) @RequestParam(defaultValue = "10") int pageSize,
                                                           @Parameter(description = "sort of the page to be find. Cannot be empty.",
                                                                   required = true) @RequestParam(defaultValue = "id") String sortBy,
                                                           @RequestParam(required = false, defaultValue = "desc") String order) {
        List<Chat> page = patientService.getListMessageUser(id);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @Operation(summary = "Liste de discution entre deux utilisateur ( le sender et le receiver)", tags = "chat", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Users.class)))),
            @ApiResponse(responseCode = "403", description = "vous n'avez pas les autorisations néccessaire pour accéder à cette resource", content = @Content(mediaType = "Application/Json"))})
 //   @PreAuthorize("@authorizationService.canUpdateOwnerItem(#receiverId,'User')")
  //  @GetMapping("/message/{receiverId}/{senderId}")
    @RequestMapping(method=RequestMethod.GET,value="/message/{receiverId}/{senderId}",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getAllMessageSenderAndReceiver(@PathVariable Long receiverId,@PathVariable Long senderId,
                                                                     @Parameter(description = "number of the page to be find. Cannot be empty.",
                                                                             required = true) @RequestParam(defaultValue = "0") int pageNo,
                                                                     @Parameter(description = "size of the page to be find. Cannot be empty.",
                                                                             required = true) @RequestParam(defaultValue = "200000") int pageSize,
                                                                     @Parameter(description = "sort of the page to be find. Cannot be empty.",
                                                                             required = true) @RequestParam(defaultValue = "id") String sortBy,
                                                                     @RequestParam(required = false, defaultValue = "desc") String order) {
        List<Chat> page = patientService.getListMessageUser(receiverId,senderId);
        List<Chat> page2 = patientService.getListMessageUser(senderId,receiverId);
        List<Chat> chatfinal = Stream.of(page, page2)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingLong(Chat::getId))
                .collect(Collectors.toList());

       PageRequest pageable =  PageRequest.of(pageNo, pageSize, Sort.by(Direction.fromString(order), sortBy));

        int max = (pageSize*(pageNo+1)>chatfinal.size())? chatfinal.size(): pageSize*(pageNo+1);

        Page<Chat> pageImpianto = new PageImpl<Chat>(chatfinal.subList(pageNo*pageSize, max), pageable, chatfinal.size());
        return new ResponseEntity<>(pageImpianto, HttpStatus.OK);
    }

    @Operation(summary = "Compter le nombre de nouveau message pour une fil de discution", tags = "chat", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content(mediaType = "Application/Json")), })
   // @PreAuthorize("@authorizationService.canUpdateOwnerItem(#receiverId,'User')")
    @GetMapping("/countNewMesssage/{receiverId}/{senderId}")
    public ResponseEntity<?> countMessage(@PathVariable() Long receiverId,@PathVariable Long senderId)  {
        return ResponseEntity.status(HttpStatus.OK).body(new NotificationCountDto(patientService.countAllMessageWhereStatusWrite(receiverId,senderId)));
    }

    @Operation(summary = "Marquer les messages d'une fil de discution comme lu (READ)", tags = "chat", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content(mediaType = "Application/Json")), })
   // @PreAuthorize("@authorizationService.canUpdateOwnerItem(#receiverId,'User')")
    @GetMapping("/changeStatusMessage/{receiverId}/{senderId}")
    public ResponseEntity<?> ChangeStatusMessageToRead(@PathVariable() Long receiverId,@PathVariable Long senderId)  {
        return ResponseEntity.status(HttpStatus.OK).body(patientService.updateStatusOfAllMessage(receiverId,senderId));
    }

    @Operation(summary = "creation d'un fichier pour le chat", tags = "Medicaments", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = UploadFileResponseDto.class)))),
            @ApiResponse(responseCode = "500", description = "Sorry! Filename contains invalid path sequence / Could not store file", content = @Content(mediaType = "Application/Json"))})
    @PostMapping("/chat/uploadfile")
    public ResponseEntity<?> uploadFile(@RequestParam(required = true) MultipartFile[] files, @RequestParam(required = true) Long senderId, @RequestParam(required = true)  Long receiverId, @RequestParam(required = false)  String fcmToken) {
        List<UploadFileResponseDto> list = new ArrayList<>();
        for (MultipartFile file : files) {
            String nameAjust = RandomStringUtils.random(20, 35, 125, true, true, null, new SecureRandom());
            String fileName = sliderDocumentService.storeFile(file, nameAjust+"-"+removeExtension(file.getOriginalFilename()));
            String fileDownloadUri = api_base_url+"/api/v1.0/user/chat/downloadFile/"+nameAjust+"-"+file.getOriginalFilename();
            Chat chat =    patientService.sendImageFromChat(fileDownloadUri,senderId,receiverId);
            Pusher pusher = new Pusher(pusherId, pusherKey, pusherSecret);
            pusher.setCluster(pusherCluster);
            pusher.setEncrypted(true);
            Map<String, String> mapping = new HashMap<>();
            mapping.put("senderId", chat.getSender().getUserId().toString());
            mapping.put("receiverId", chat.getReceiver().getUserId().toString());
            mapping.put("messageType", chat.getMessageType().name());
            mapping.put("dateEnvois", chat.getCreatedAt().toString());
            pusher.trigger("channel", "chat", mapping);

            Map<String, String> pushData = new HashMap<>();
            PushNotificationRequest request = new PushNotificationRequest();
            request.setTitle("Nouveau message");
            request.setMessage(chat.getMessage());
            //request.setTopic("com.dev.sesa");
            request.setToken(fcmToken!=null?fcmToken:null);
            pushData.put("title", "Nouveau message");
            pushData.put("message", "un nouveau fichier envoyé dans le chat");
            pushData.put("messageType",chat.getMessageType().name());
            pushData.put("dateEnvois",chat.getCreatedAt().toString());
            pushData.put("receiverId",chat.getReceiver().getUserId().toString());
            pushData.put("senderId",chat.getSender().getUserId().toString());
            // pushData.put("image", "https://raw.githubusercontent.com/Firoz-Hasan/SpringBootPushNotification/master/pushnotificationconcept.png");
            pushData.put("timestamp", chat.getCreatedAt().toString());
            // pushData.put("article_data", "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
            pushNotificationService.sendPushNotification(pushData,request);
            UploadFileResponseDto uploadFileResponseDto = new  UploadFileResponseDto(fileName, fileDownloadUri, file.getContentType(), file.getSize());
            list.add(uploadFileResponseDto);

        }

        return ResponseEntity.ok(list);
    }

    public static String removeExtension(String fileName) {
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }

    }

    @Operation(summary = "récuperer un fichier pour un chat", tags = "Medicaments", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content(mediaType = "Application/Json")), })
    @GetMapping(value = "/chat/downloadFile/{filename}")
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
}
*/
