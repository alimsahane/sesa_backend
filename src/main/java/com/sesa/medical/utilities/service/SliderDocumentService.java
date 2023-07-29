/*
package com.sesa.medical.utilities.service;

import com.sesa.medical.users.entities.DocumentStorageProperties;
import com.sesa.medical.users.exception.DocumentsStorageException;
import com.sesa.medical.users.repository.IDocumentStoragePropertiesRepo;
import com.sesa.medical.users.services.IUserService;
import com.sesa.medical.utilities.repository.SliderDocumentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Transactional
public class SliderDocumentService {

    private final Path fileStorageLocation;

    @Autowired
    SliderDocumentRepo sliderDocumentRepo;


    @Autowired
    public SliderDocumentService(DocumentStorageProperties fileStorageProperties) {
        fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception e) {
            throw new DocumentsStorageException("Could not create the directory where the uploaded files will be stored.", e);

        }
    }


    public String storeFile(MultipartFile file, String docType) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = "";

        try {
            if(originalFilename.contains("..")) {
                throw new DocumentsStorageException("Sorry! Filename contains invalid path sequence " + originalFilename);
            }
            String fileExtension = "";
            try {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } catch (Exception e) {
                fileExtension = "";
            }
            fileName =  docType + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new DocumentsStorageException("Could not store file " + fileName + ". Please try again!", e);
        }
    }



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


    public String getDocumentName(String docType) {
        return sliderDocumentRepo.getUploadDocumnetPath(docType);
    }


}
*/
