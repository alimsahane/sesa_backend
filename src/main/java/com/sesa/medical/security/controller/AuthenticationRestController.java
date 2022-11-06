package com.sesa.medical.security.controller;

import com.sesa.medical.globalconfig.ApplicationConstant;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.medecin.service.IDoctorService;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.security.TokenProvider;
import com.sesa.medical.security.UserPrincipal;
import com.sesa.medical.security.dto.*;
import com.sesa.medical.security.jwt.JwtUtils;
import com.sesa.medical.security.oauth2.CurrentUser;
import com.sesa.medical.security.oauth2.user.OAuth2UserInfo;
import com.sesa.medical.users.dto.UserReqDto;
import com.sesa.medical.users.dto.UserRestDto;
import com.sesa.medical.users.entities.EStatusUser;
import com.sesa.medical.users.entities.OldPassword;
import com.sesa.medical.users.entities.Users;
import com.sesa.medical.users.repository.IUsersRepository;
import com.sesa.medical.users.services.IUserService;
import com.sesa.medical.users.services.imp.SendingMailService;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
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
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@OpenAPIDefinition(info = @Info(title = "API SeSa App", description = "Documentation de l'API", version = "1.0"))
//@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Tag(name = "authentification")
@RequestMapping("/api/v1.0/auth")
@Slf4j
public class AuthenticationRestController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    IUserService userService;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    IDoctorService doctorService;

    @Autowired
    SendingMailService sendingMailService;

    @Autowired
    private SecretGenerator secretGenerator;

    @Autowired
    private QrDataFactory qrDataFactory;

    @Autowired
    private QrGenerator qrGenerator;

    @Autowired
    private CodeVerifier verifier;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    IUsersRepository usersRepository;
    @Autowired
    IPatientService patientService;
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    IUsersRepository usersRepo;
    @Value("${app.front-reset-password-page}")
    String urlResetPasswordPage;
    @Value("${app.api-confirm-account-url}")
    String urlConfirmAccount;
    @Value("${app.api-confirm-code-url}")
    String urlConfirmCode;

    @Operation(summary = "Inscription sur l'application", tags = "users", responses = {
            @ApiResponse(responseCode = "201", description = "User crée avec succès", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = UserRestDto.class)))),
            @ApiResponse(responseCode = "400", description = "Erreur: Ce nom d'utilisateur est déjà utilisé/Erreur: Cet email est déjà utilisé", content = @Content(mediaType = "Application/Json")),})
    @PostMapping("/sign-up")
    public ResponseEntity<Object> add(@Valid @RequestBody UserReqDto userAddDto, HttpServletRequest request) {
        if (userService.existsByEmail(userAddDto.getEmail(), null)) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("messages.email_exists", null, LocaleContextHolder.getLocale())));
        }
        if (userService.existsByUsername(userAddDto.getUsername(), null)) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("messages.username_exists", null, LocaleContextHolder.getLocale())));
        }
        if (userService.existsByTel(userAddDto.getTel1(), null)) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("messages.phone_exists", null, LocaleContextHolder.getLocale())));
        }
        Users u = modelMapper.map(userAddDto, Users.class);
        u.setUsing2FA(false);
        u.setFirstName(userAddDto.getFirstName());
        u.setLastName(userAddDto.getLastName());
        u.setCreatedAt(LocalDateTime.now());
        String token = "";
        Users user = new Users();
        if (userAddDto.getUserType().equals("medecin")) {
            user = doctorService.saveDoctor(u);
        } else {
            user = patientService.savePatient(u);
        }
        if(userAddDto.getVerificationType().equals("email")) {
            token = tokenProvider.createTokenRefresh(user, true);
            userService.updateAuthToken(user.getUserId(), token);
            EmailVerificationDto emailVerificationDto = new EmailVerificationDto();
            emailVerificationDto.setCode(urlConfirmAccount + token);
            emailVerificationDto.setTo(user.getEmail());
            emailVerificationDto.setObject(ApplicationConstant.SUBJECT_EMAIL_VERIF);
            emailVerificationDto.setTemplate(ApplicationConstant.TEMPLATE_EMAIL_VERIF);
            jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_EMAIL_VERIFICATION,emailVerificationDto);
        } else {
            token = String.valueOf(tokenProvider.generateOtpCode());
            userService.updateOtpCode(user.getUserId(), token);
            OtpCodeDto otpCodeDto = new OtpCodeDto();
            otpCodeDto.setCode(user.getOtpCode());
            otpCodeDto.setTel(user.getTel1());
            jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_SMS_OTP,otpCodeDto);
        }
       // sendingMailService.sendVerificationMail(user.getEmail(), ApplicationConstant.getSiteURL(request) + "/api/v1.0/auth/user/confirm-account?code=" + token);
        UserRestDto userResDto = modelMapper.map(user, UserRestDto.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(userResDto);
    }

    @Operation(summary = "Vérification de l'adresse mail  de l'utilisateur", tags = "authentification", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = AuthResDto.class)))),
            @ApiResponse(responseCode = "400", description = "", content = @Content(mediaType = "Application/Json"))})
    @GetMapping("/user/confirm-account-email")
    public ResponseEntity<Object> confirmUserAccount(@RequestParam(value = "code",required = true) String code,
                                                     @RequestParam(required = false) String tel) {
        Users user;
            try {
                user = getUser(code);
            } catch (ExpiredJwtException e) {
                log.error("JWT token is expired: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(messageSource.getMessage("messages.token_expired", null, LocaleContextHolder.getLocale()));
            }


        if (user.getStatus().getName() == EStatusUser.USER_ENABLED) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(HttpStatus.BAD_REQUEST, messageSource.getMessage("messages.email_already_checked", null, LocaleContextHolder.getLocale())));
        }
        if (code.equals(user.getTokenAuth())  && user.getStatus().getName() == EStatusUser.USER_DISABLED) {
            userService.editToken(user.getUserId(), null);
            activeSpecifiqueUsers(user);
            userService.changeStatusEmailVerify(user);
            String refreshToken = tokenProvider.createTokenRefresh(user, true);
            String bearerToken = tokenProvider.createTokenLocalUser(user, true);
            List<String> roles = user.getRoles().stream().map(item -> item.getName().name())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new AuthResDto(bearerToken, refreshToken, "Bearer", roles, true));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponseDto(HttpStatus.UNAUTHORIZED,messageSource.getMessage("messages.email_verified_failure", null, LocaleContextHolder.getLocale()))
                );
    }
    @Operation(summary = "Vérification  du numero de téléphone de l'utilisateur", tags = "authentification", responses = {
            @ApiResponse(responseCode = "201", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = AuthResDto.class)))),
            @ApiResponse(responseCode = "400", description = "", content = @Content(mediaType = "Application/Json"))})
    @PostMapping("/user/confirm-account-phone")
    public ResponseEntity<Object> confirmUserAccount(@Valid @RequestBody ConfirmAccountPhoneDto confirmAccountPhoneDto) {
        Users user;
        if (confirmAccountPhoneDto.getCode().length() == 4) {
            user = userService.getByTel(confirmAccountPhoneDto.getTel()).orElseThrow(() -> new ResourceNotFoundException(
                    messageSource.getMessage("messages.user_not_found", null, LocaleContextHolder.getLocale())));
            if (confirmAccountPhoneDto.getCode().equals(user.getOtpCode()) && ChronoUnit.MINUTES.between(user.getOtpCreatedAt(), LocalDateTime.now()) > 5) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponseDto(HttpStatus.UNAUTHORIZED,messageSource.getMessage("messages.code_expired", null, LocaleContextHolder.getLocale())));
            }
        } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponseDto(HttpStatus.UNAUTHORIZED,messageSource.getMessage("messages.code_not_valid", null, LocaleContextHolder.getLocale())));

        }

        if (user.getStatus().getName() == EStatusUser.USER_ENABLED) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(HttpStatus.BAD_REQUEST, messageSource.getMessage("messages.email_already_checked", null, LocaleContextHolder.getLocale())));
        }

        if(confirmAccountPhoneDto.getCode().equals(user.getOtpCode()) && user.getStatus().getName() == EStatusUser.USER_DISABLED) {
            userService.updateOtpCode(user.getUserId(),null);
            activeSpecifiqueUsers(user);
            userService.changeStatusPhoneVerify(user);
            String refreshToken = tokenProvider.createTokenRefresh(user, true);
            String bearerToken = tokenProvider.createTokenLocalUser(user, true);
            List<String> roles = user.getRoles().stream().map(item -> item.getName().name())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new AuthResDto(bearerToken, refreshToken, "Bearer", roles, true));

            /*return ResponseEntity.ok().body(
                    new MessageResponseDto(HttpStatus.OK, messageSource.getMessage("messages.email_verified_success", null, LocaleContextHolder.getLocale()))
            );*/
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponseDto(HttpStatus.UNAUTHORIZED,messageSource.getMessage("messages.email_verified_failure", null, LocaleContextHolder.getLocale()))
        );
    }

   void  activeSpecifiqueUsers(Users user) {
      if (user.getClass().equals(Doctors.class)) {
          userService.editStatus(user.getUserId(), Long.valueOf(EStatusUser.USER_ENABLED.ordinal() + 1L));
      }
      if (user.getClass().equals(Patient.class)) {
          userService.editStatus(user.getUserId(), Long.valueOf(EStatusUser.USER_ENABLED.ordinal() + 1L));
      }
    }

    @Operation(summary = "Authentifier un utilisateur", tags = "authentification", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = AuthResDto.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "401", description = "", content = @Content(mediaType = "Application/Json"))})
    @PostMapping("/sign-in")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody SignInDto userAuthDto) throws Exception {
        Users user = new Users(userAuthDto.getUsername(), userAuthDto.getPassword());
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal.getStatus().getName() == EStatusUser.USER_ENABLED) {
            boolean authenticated = userPrincipal.isUsing2FA();
            if (authenticated == false) {
                String refreshToken = tokenProvider.createTokenRefresh(authentication, true);
                String bearerToken = tokenProvider.createToken(authentication, true);
                List<String> roles = userPrincipal.getAuthorities().stream().map(item -> item.getAuthority())
                        .collect(Collectors.toList());
                Users users = userService.getUsernameOrEmailOrTel1(userAuthDto.getUsername(), userAuthDto.getUsername(), userAuthDto.getUsername());
                userService.editToken(users.getUserId(), refreshToken);
                log.info("user " + userPrincipal.getUsername() + " authenticated");
                return ResponseEntity.ok(new AuthResDto(bearerToken, refreshToken, "Bearer", roles, true));
            } else {
                if(userAuthDto.getAppProvider().equals("mobile")) {
                    String code = String.valueOf(tokenProvider.generateOtpCode());
                    Users userUpdate = updateExistingUser(userPrincipal.getId(), code, userAuthDto.getAppProvider());
                    String tel = userUpdate.getTel1() != null ? userUpdate.getTel1() : String.valueOf(userUpdate.getTel2());
                    String bearerToken = tokenProvider.createToken(authentication, false);
                    OtpCodeDto otpCodeDto = new OtpCodeDto();
                    otpCodeDto.setCode(code);
                    otpCodeDto.setTel(tel);
                    jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_SMS_OTP,otpCodeDto);
                    return ResponseEntity.ok().body(new SignUpResponse(true, messageSource.getMessage("messages.code-otp", null, LocaleContextHolder.getLocale()), bearerToken, false));
                } else {
                    try {
                        Users userUpdate = updateExistingUser(userPrincipal.getId(), secretGenerator.generate(), userAuthDto.getAppProvider());
                        String bearerToken = tokenProvider.createToken(authentication, false);
                        QrData data = qrDataFactory.newBuilder().label(userUpdate.getEmail()).secret(userUpdate.getSecret()).issuer(userUpdate.getUsername()).build();
                        // Generer un QR code d'image en base64 qui sera utilisé pour recuperer le code à partie d'une app comme google authenticator par exemple
                        // il sera utilisé dans un Tag  <img> :
                        String qrCodeImage = getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
                        return ResponseEntity.ok().body(new SignUpResponse(true, qrCodeImage, bearerToken, false));
                    } catch (QrGenerationException e) {
                        log.error("QR Generation Exception Ocurred", e);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body((new MessageResponseDto(HttpStatus.BAD_REQUEST, "Unable to generate QR code!")));
                    }
                }

            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponseDto(HttpStatus.UNAUTHORIZED,messageSource.getMessage("messages.account-not-active", null, LocaleContextHolder.getLocale() )));
    }


    private Users updateExistingUser(Long id,String code,String appProvider) {
        Users existingUser = usersRepository.getOne(id);
        if(appProvider.equals("mobile")) {
            existingUser.setOtpCode(code);
            existingUser.setOtpCreatedAt(LocalDateTime.now());
        } else {
            existingUser.setSecret(code);
        }
        return usersRepository.save(existingUser);
    }

    @Operation(summary = "Vérification du code d'authentification à 2FA", tags = "authentification", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Users.class)))),
            @ApiResponse(responseCode = "400", description = "code de vérification incorrect", content = @Content(mediaType = "Application/Json"))})
    @GetMapping("/verify/{code}")
    @PreAuthorize("hasRole('PRE_VERIFICATION_USER')")
    public ResponseEntity<?> verifyCode(@NotEmpty @PathVariable String code,@NotEmpty @RequestParam(name = "appProvider",required = true) String appProvider, @CurrentUser UserPrincipal userPrincipal) {
        Users users = userService.getByEmail(userPrincipal.getEmail());
        if(appProvider.equals("web")) {
            if (!verifier.isValidCode(users.getSecret(), code)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((new MessageResponseDto(HttpStatus.BAD_REQUEST, " QR code invalid!")));
            }
        } else {
            if (code.equals(users.getOtpCode()) && ChronoUnit.MINUTES.between(users.getOtpCreatedAt(), LocalDateTime.now()) > 5) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(messageSource.getMessage("messages.code_expired", null, LocaleContextHolder.getLocale()));
            }
        }
        String bearerToken = tokenProvider.createTokenLocalUser(users, true);
        String refreshToken = tokenProvider.createTokenRefresh(users, true);
        userService.editToken(userPrincipal.getId(), refreshToken);
        List<String> roles = userPrincipal.getAuthorities().stream().map(item -> item.getAuthority())
                .collect(Collectors.toList());
        updateExistingUser(users.getUserId(), null, appProvider);
        log.info("user " + userPrincipal.getUsername() + " authenticated");
        return ResponseEntity.ok(new AuthResDto(bearerToken, refreshToken, "Bearer", roles, true));
    }

    @Operation(summary = "Renvoyer le code OTP pour vérification 2FA", tags = "authentification", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Users.class)))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST", content = @Content(mediaType = "Application/Json"))})
    @GetMapping("/resetOtpCode2Fa")
    @PreAuthorize("hasRole('PRE_VERIFICATION_USER')")
    public ResponseEntity<?> resetCode(@NotEmpty @RequestParam(name = "appProvider",required = true) String appProvider, @CurrentUser UserPrincipal userPrincipal) {
        Users users = userService.getByEmail(userPrincipal.getEmail());
        if(appProvider.equals("web")) {
            try {
                Users userUpdate = updateExistingUser(userPrincipal.getId(), secretGenerator.generate(), appProvider);
                String bearerToken = tokenProvider.createTokenLocalUser(users, false);
                QrData data = qrDataFactory.newBuilder().label(userUpdate.getEmail()).secret(userUpdate.getSecret()).issuer(userUpdate.getUsername()).build();
                // Generer un QR code d'image en base64 qui sera utilisé pour recuperer le code à partie d'une app comme google authenticator par exemple
                // il sera utilisé dans un Tag  <img> :
                String qrCodeImage = getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
                return ResponseEntity.ok().body(new SignUpResponse(true, qrCodeImage, bearerToken, false));
            } catch (QrGenerationException e) {
                log.error("QR Generation Exception Ocurred", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body((new MessageResponseDto(HttpStatus.BAD_REQUEST, "Unable to generate QR code!")));
            }
        } else {
            String code = String.valueOf(tokenProvider.generateOtpCode());
            Users userUpdate = updateExistingUser(userPrincipal.getId(), code, appProvider);
            String tel = userUpdate.getTel1() != null ? userUpdate.getTel1() : String.valueOf(userUpdate.getTel2());
            String bearerToken = tokenProvider.createTokenLocalUser(users, false);
            OtpCodeDto otpCodeDto = new OtpCodeDto();
            otpCodeDto.setCode(code);
            otpCodeDto.setTel(tel);
            jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_SMS_OTP,otpCodeDto);
            return ResponseEntity.ok().body(new SignUpResponse(true, messageSource.getMessage("messages.code-otp", null, LocaleContextHolder.getLocale()), bearerToken, false));
        }
    }

    @Operation(summary = "Renvoyer le code OTP pour confirmation de compte", tags = "authentification", responses = {
            @ApiResponse(responseCode = "201", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Users.class)))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST", content = @Content(mediaType = "Application/Json"))})
    @PostMapping("/resetOtpCode")
    public ResponseEntity<?> resetCodeConfirmAccount(@Valid @RequestBody ResetOtpCodeDto resetOtpCodeDto) {
        Optional<Users> users = userService.getByTel(resetOtpCodeDto.getTel());
        if(users.get().getStatus().getName().equals(EStatusUser.USER_ENABLED)) {
            return ResponseEntity.ok().body(new MessageResponseDto(HttpStatus.OK, " your account has already been activated !"));
        }
        if(resetOtpCodeDto.getAppProvider().equals("web")) {
            try {
                Users userUpdate = updateExistingUser(users.get().getUserId(), secretGenerator.generate(), resetOtpCodeDto.getAppProvider());
                String bearerToken = tokenProvider.createTokenLocalUser(users.get(), false);
                QrData data = qrDataFactory.newBuilder().label(userUpdate.getEmail()).secret(userUpdate.getSecret()).issuer(userUpdate.getUsername()).build();
                // Generer un QR code d'image en base64 qui sera utilisé pour recuperer le code à partie d'une app comme google authenticator par exemple
                // il sera utilisé dans un Tag  <img> :
                String qrCodeImage = getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
                return ResponseEntity.ok().body(new SignUpResponse(true, qrCodeImage, bearerToken, false));
            } catch (QrGenerationException e) {
                log.error("QR Generation Exception Ocurred", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body((new MessageResponseDto(HttpStatus.BAD_REQUEST, "Unable to generate QR code!")));
            }
        } else {
            String code = String.valueOf(tokenProvider.generateOtpCode());
            Users userUpdate = updateExistingUser(users.get().getUserId(), code, resetOtpCodeDto.getAppProvider());
            String tel1 = userUpdate.getTel1() != null ? userUpdate.getTel1() : String.valueOf(userUpdate.getTel2());
            OtpCodeDto otpCodeDto = new OtpCodeDto();
            otpCodeDto.setCode(code);
            otpCodeDto.setTel(tel1);
            jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_SMS_OTP,otpCodeDto);
            return ResponseEntity.ok().body(new MessageResponseDto(HttpStatus.OK, " Code Otp generated successful !"));
        }
    }

    @Operation(summary = "rafraichir le token de l'utilisateur", tags = "authentification", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = AuthRefreshResp.class)))),
            @ApiResponse(responseCode = "401", description = "Le token a été revoqué", content = @Content(mediaType = "Application/Json"))})
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshDto request) throws Exception {
        String token = request.getRefreshToken();
        Users user = getUser(token);
        if (token.equals(user.getTokenAuth())) {
            String newBearerToken = tokenProvider.createTokenLocalUser(user, true);
            return ResponseEntity.ok(new AuthRefreshResp(newBearerToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponseDto(HttpStatus.UNAUTHORIZED, "this token has been revoked"));
    }

    private Users getUser(String token) {
        Long id = tokenProvider.getUserIdFromToken(token);
        return userService.getById(id);
    }

    @Operation(summary = "Information sur un utilisateur", tags = "authentification", responses = {
            @ApiResponse(responseCode = "200", description = "Succès de l'opération", content = @Content(mediaType = "Application/Json", array = @ArraySchema(schema = @Schema(implementation = Users.class)))),
            @ApiResponse(responseCode = "401", description = "", content = @Content(mediaType = "Application/Json"))})
    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public Users getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userService.getById(userPrincipal.getId());
    }

    @Operation(summary = "modifier le password d'un utilisateur", tags = "users", responses = {
            @ApiResponse(responseCode = "200", description = "Mot de passe changé avec succès", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Erreur: Ancien mot de passe incorrect", content = @Content(mediaType = "Application/Json"))})
    @PreAuthorize("@authorizationService.canUpdateOwnerItem(#id, 'User')")
    @PutMapping("/user/{id}/password-update")
    public ResponseEntity<?> editPassword(@PathVariable Long id, @Valid @RequestBody UserEditPasswordDto userEditPasswordDto) {
        Users user = userService.getById(id);
        if (!BCrypt.checkpw(userEditPasswordDto.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new DefaultResponseDto("Ancien mot de passe incorrect", HttpStatus.BAD_REQUEST));
        }
        List<OldPassword> oldPasswords = user.getOldPasswords();
        for (OldPassword oldPassword : oldPasswords) {
            if (BCrypt.checkpw(userEditPasswordDto.getPassword(), oldPassword.getPassword())) {
                return ResponseEntity.badRequest().body(new DefaultResponseDto("Mot de passe déjà utilisé par le passé", HttpStatus.BAD_REQUEST));
            }
        }
        userService.editPassword(user, userEditPasswordDto);
        return ResponseEntity.ok(new DefaultResponseDto("Mot de passe réinitialisé avec succès ", HttpStatus.OK));
    }

    @Operation(summary = "Réinitialiser son mot de passe etape 1 (verification du user)", tags = "users", responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "L'utilisateur n'existe pas dans la BD", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Erreur dans le format de la requete", content = @Content(mediaType = "Application/Json")) })
    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto)
            throws Exception {
        if (resetPasswordDto.getLogin() == null || resetPasswordDto.getLogin().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("messages.requete_incorrect", null, LocaleContextHolder.getLocale())));
        }
        Users user = userService.checkUserAndGenerateCode(resetPasswordDto.getLogin());
        if (resetPasswordDto.getLogin().contains("@")) {
            EmailResetPasswordDto emailVerificationDto = new EmailResetPasswordDto();
            emailVerificationDto.setCode(urlConfirmCode + user.getTokenAuth());
            emailVerificationDto.setTo(user.getEmail());
            emailVerificationDto.setUsername(user.getUsername());
            emailVerificationDto.setObject(ApplicationConstant.SUBJECT_PASSWORD_RESET);
            emailVerificationDto.setTemplate(ApplicationConstant.TEMPLATE_PASSWORD_RESET);
            jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_EMAIL_RESET_PASSWORD,emailVerificationDto);
        } else {
            String tel1 = user.getTel1() != null ? user.getTel1() : String.valueOf(user.getTel2());
            OtpCodeDto otpCodeDto = new OtpCodeDto();
            otpCodeDto.setCode(user.getOtpCode());
            otpCodeDto.setTel(tel1);
            jmsTemplate.convertAndSend(ApplicationConstant.PRODUCER_SMS_OTP,otpCodeDto);
        }
        return ResponseEntity.ok().body(new MessageResponseDto(HttpStatus.OK,
                messageSource.getMessage("messages.code_sent_success", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Réinitialiser son mot de passe 2 (confirmation du code pour le web)", tags = "users", description = "La validation du token est requis pour le client web", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "L'utilisateur n'existe pas dans la BD", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Erreur dans le format de la requete", content = @Content(mediaType = "Application/Json")) })

    @GetMapping("/confirm-code-email")
    public ResponseEntity<Object> resetPassword(@RequestParam String code, @RequestParam(required = false) String tel,
                                                @RequestBody(required = false) UserResetPassword userResetPwd) throws Exception {
        Users user;
            try {
                user = getUser(code);
            } catch (ExpiredJwtException e) {
                log.error("JWT token is expired: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponseDto(HttpStatus.UNAUTHORIZED,messageSource.getMessage("messages.code_expired", null, LocaleContextHolder.getLocale())));
            }
            if (code.equals(user.getTokenAuth())) {
                String newUrl = urlResetPasswordPage + LocaleContextHolder.getLocale().getLanguage() + "/" + code;
                return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header(HttpHeaders.LOCATION, newUrl).build();
            }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponseDto(HttpStatus.UNAUTHORIZED,messageSource.getMessage("messages.unauthorized", null, LocaleContextHolder.getLocale())));
    }
    @Operation(summary = "Réinitialiser son mot de passe 2 (enrégistrement du password pour le mobile)", tags = "users", description = "Le téléphone et le nouveau password sont requis pour le client mobile", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "L'utilisateur n'existe pas dans la BD", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Erreur dans le format de la requete", content = @Content(mediaType = "Application/Json")) })

    @PostMapping("/confirm-code-phone")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody UserResetPassword userResetPassword) throws Exception {
        Users user;
        if (userResetPassword.getCode().length() == 4) {
            user = userService.getByTel(userResetPassword.getTel()).orElseThrow(() -> new ResourceNotFoundException(
                    messageSource.getMessage("messages.user_not_found", null, LocaleContextHolder.getLocale())));
            if (userResetPassword.getCode().equals(user.getOtpCode()) && ChronoUnit.HOURS.between(user.getOtpCreatedAt(), LocalDateTime.now()) > 10) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponseDto(HttpStatus.UNAUTHORIZED,messageSource.getMessage("messages.code_expired", null, LocaleContextHolder.getLocale())));
            }
            if (userResetPassword.getCode().equals(user.getOtpCode())) {
                Users user2 = userService.resetPassword(user, userResetPassword.getPassword());
                return ResponseEntity.ok(new MessageResponseDto(HttpStatus.OK,messageSource.getMessage("messages.password_reset_successful", null, LocaleContextHolder.getLocale())));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDto(HttpStatus.UNAUTHORIZED,messageSource.getMessage("messages.code_not_valid", null, LocaleContextHolder.getLocale())));

        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponseDto(HttpStatus.UNAUTHORIZED,messageSource.getMessage("messages.unauthorized", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Réinitialiser son mot de passe 3 enrégistrement du password(pour le web uniquement)", tags = "users", responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "404", description = "L'utilisateur n'existe pas dans la BD", content = @Content(mediaType = "Application/Json")),
            @ApiResponse(responseCode = "400", description = "Mot de passe déjà utilisé par le passé", content = @Content(mediaType = "Application/Json")) })

    @PutMapping("/reset-password")
    public ResponseEntity<Object> resetPassword2(@RequestParam String code,@Valid @RequestBody UserResetPassword userResetPwd)
            throws Exception {
        Users user = usersRepo.findByTokenAuth(code).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("messages.user_not_found", null, LocaleContextHolder.getLocale())));
        List<OldPassword> oldPasswords = user.getOldPasswords();
        for (OldPassword oldPassword : oldPasswords) {
            if (BCrypt.checkpw(userResetPwd.getPassword(), oldPassword.getPassword())) {
                return ResponseEntity.badRequest().body(new MessageResponseDto(HttpStatus.BAD_REQUEST, messageSource.getMessage("messages.password_already_use", null,
                        LocaleContextHolder.getLocale())));
            }
        }
        Users user2 = userService.resetPassword(user, userResetPwd.getPassword());
        return ResponseEntity.ok(new MessageResponseDto(HttpStatus.OK,messageSource.getMessage("messages.password_reset_successful", null, LocaleContextHolder.getLocale())));
    }


}
