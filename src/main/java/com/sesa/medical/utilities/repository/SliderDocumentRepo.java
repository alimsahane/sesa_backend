package com.sesa.medical.utilities.repository;

import com.sesa.medical.utilities.entities.SliderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SliderDocumentRepo extends JpaRepository<SliderDocument,Long> {
    @Query(value = "Select file_name from slider_document a where document_type = ?1", nativeQuery = true)
    String getUploadDocumnetPath(String docType);
}
