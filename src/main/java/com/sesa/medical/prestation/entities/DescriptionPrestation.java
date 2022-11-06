package com.sesa.medical.prestation.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DescriptionPrestation {

    @Schema(description = "identifiant de la description", example = "1", required = true, accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    private LocalDateTime createdAt;
}
