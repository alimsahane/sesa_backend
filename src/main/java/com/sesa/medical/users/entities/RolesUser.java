package com.sesa.medical.users.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RolesUser {
    @Schema(description = "identifiant unique du role", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Schema(description = "nom du role", example = "ROLE_ADMIN")
    @Enumerated(EnumType.STRING)
    private ERoles name;

    public RolesUser(ERoles name) {
        this.name = name;
    }
}
