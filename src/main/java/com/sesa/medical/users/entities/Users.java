package com.sesa.medical.users.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sesa.medical.patient.entities.Chat;
import com.sesa.medical.security.dto.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@EqualsAndHashCode(of = { "username", "email", "password" })
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "users")
public class Users {
    @Schema(description = "identifiant unique de l'utilisateur", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long userId;

    @Schema(description = "nom d'utilisateur", example = "warren")
    @Column(unique = true)
    private String username;

    @Column(unique = true)
    @Email
    private String email;
    private String firstName;
    private String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate birthdate;

    private String birthdatePlace;

    @Size(max = 1)
    private String sexe;

    private String maritalStatus;

    private String nationality;

    @Schema(description = "mot de passe de l'utilisateur", example = "Warren@90")
    @JsonIgnore
    private String password;

    @Column(nullable = true, unique = true)
    private String tel1;

    @Column(nullable = true, unique = true)
    private String tel2;

    @JsonIgnore
    private String tokenAuth;	// utilisé pour vérifier l'activation du compte d'une part et la non revocation du refesh tokenAuth d'autre part

    @JsonIgnore
    private String notificationKey;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<RolesUser> roles;

    @OneToOne(mappedBy = "users")
    private Adresses adresses;
    @ManyToOne
    private StatusUsers status;

    @ManyToMany
    @JsonIgnore
    private List<OldPassword> oldPasswords = new ArrayList<>();

    @Transient
    @Schema(hidden = true)
    @JsonIgnore
    private List<ERoles> roleNames;
    @Transient
    @Schema(hidden = true)
    @JsonIgnore
    private String providerName;
    private boolean emailVerify = false;
    private boolean phoneVerify = false;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    private String imageUrl;
    private String providerId;

    @Column(name = "USING_2FA")
    private boolean using2FA ;
    private String otpCode;
    private LocalDateTime otpCreatedAt;
    private String secret;
    private boolean isDelete = false;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updateAt;

    private String fcmToken;

    @OneToMany(mappedBy = "receiver")
    @JsonIgnore
    private Collection<Chat> receiverMessages;
    @OneToMany(mappedBy = "sender")
    @JsonIgnore
    private Collection<Chat> sendMessages;
    public Users(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }


    public Users(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }
}
