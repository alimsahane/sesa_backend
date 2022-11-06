package com.sesa.medical.users.entities;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.persistence.*;


@Entity
@Getter
@Setter
@ConfigurationProperties(prefix = "file")
@NoArgsConstructor
@EqualsAndHashCode(of = { "documentId", "fileName", "documentType", "documentFormat", "user" })
@Table(name = "user_documents")
public class DocumentStorageProperties {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long documentId;

    private String fileName;

    private String documentType;

    private String documentFormat;

    @Transient
    private String uploadDir;

    @ManyToOne
    private Users user;
}
