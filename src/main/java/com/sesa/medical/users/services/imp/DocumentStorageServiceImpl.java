package com.sesa.medical.users.services.imp;

import com.sesa.medical.users.entities.DocumentStorageProperties;
import com.sesa.medical.users.exception.DocumentsStorageException;
import com.sesa.medical.users.repository.IDocumentStoragePropertiesRepo;
import com.sesa.medical.users.services.IDocumentStorageService;
import com.sesa.medical.users.services.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class DocumentStorageServiceImpl implements IDocumentStorageService {

    private final Path fileStorageLocation;
    @Autowired
    IDocumentStoragePropertiesRepo docStorageRepo;

    @Autowired
    IUserService customerService;
    
    @Autowired
    public DocumentStorageServiceImpl(DocumentStorageProperties fileStorageProperties) {
        fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception e) {
            throw new DocumentsStorageException("Could not create the directory where the uploaded files will be stored.", e);

        }
    }
    @Override
    @Transactional
    public String storeFile(MultipartFile file, Long userId, String docType) {
        // Normalize file name
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = "";

        try {
            // Check if the file's name contains invalid characters
            if(originalFilename.contains("..")) {
                throw new DocumentsStorageException("Sorry! Filename contains invalid path sequence " + originalFilename);
            }
            String fileExtension = "";
            try {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } catch (Exception e) {
                fileExtension = "";
            }
            fileName = userId + "_" + docType + fileExtension;
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            DocumentStorageProperties doc = docStorageRepo.checkDocumentByUserId(userId, docType);
            if(doc != null) {
                doc.setDocumentFormat(file.getContentType());
                doc.setFileName(fileName);
                docStorageRepo.save(doc);
            } else {
                DocumentStorageProperties newDoc = new DocumentStorageProperties();
                newDoc.setUser(customerService.getById(userId));
                newDoc.setDocumentFormat(file.getContentType());
                newDoc.setFileName(fileName);
                newDoc.setDocumentType(docType);
                docStorageRepo.save(newDoc);
            }
            return fileName;
        } catch (IOException e) {
            throw new DocumentsStorageException("Could not store file " + fileName + ". Please try again!", e);
        }
    }

    

    @Override
    @Transactional
    public Resource loadFileAsResource(String fileName) throws Exception {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }

        } catch (MalformedURLException e) {
            throw new FileNotFoundException("File not found " + fileName);
        }
    }

    @Override
    public String getDocumentName(Long userId, String docType) {
        return docStorageRepo.getUploadDocumnetPath(userId, docType);
    }
}
