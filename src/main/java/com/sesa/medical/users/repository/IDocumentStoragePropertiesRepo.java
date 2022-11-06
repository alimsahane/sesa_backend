package com.sesa.medical.users.repository;

import com.sesa.medical.users.entities.DocumentStorageProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IDocumentStoragePropertiesRepo  extends JpaRepository<DocumentStorageProperties, Long> {

    @Query(value = "Select * from user_documents where user_user_id = ?1 and document_type = ?2", nativeQuery = true)
    DocumentStorageProperties checkDocumentByUserId(Long userId, String docType);

    @Query(value = "Select file_name from user_documents a where user_user_id = ?1 and document_type = ?2", nativeQuery = true)
    String getUploadDocumnetPath(Long userId, String docType);
}
