package com.sesa.medical.users.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IDocumentStorageService {
    String storeFile(MultipartFile file, Long userId, String docType);

    Resource loadFileAsResource(String fileName) throws Exception;

    String getDocumentName(Long customerId, String docType);
}
