package com.sesa.medical.users.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class StatusUsers {
    @Schema(description = "identifiant unique du statut", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long statusId;

    @Schema(description = "nom du statut", example = "STATUS_ENABLED")
    @Enumerated(EnumType.STRING)
    private EStatusUser name;

    private String description;

    public StatusUsers(EStatusUser name, String description) {
        this.name = name;
        this.description = description;
    }
}
