package com.medicare_health_systems.service;

import com.medicare_health_systems.exceptions.ResourceNotFound;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${application.file.upload-dir}")
    private String uploadDir;

    @Value("${application.file.allowed-types}")
    private String allowedTypes;

    @Value("${application.file.max-file-size-bytes}")
    private long maxFileSizeBytes;

    private Path uploadPath;


    @PostConstruct
    public void init(){
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try{
            Files.createDirectories(uploadPath);
            log.info("File Storage initialized at {}", uploadPath);
        }catch (Exception e){
            throw new RuntimeException("Could not initialize directory!!");
        }
    }

    //store a file on disk
    public String storeFile(MultipartFile file){
        if(file.isEmpty()){
            throw new IllegalArgumentException("Cannot store empty file");
        }

        //MIME type validation
        String contentType = file.getContentType();
        List<String> type = Arrays.asList(allowedTypes.split(","));

        if(contentType == null || !allowedTypes.contains(contentType)){
            throw new IllegalArgumentException("File type not allowed. Only upload "+ allowedTypes);
        }

        //size-validation
        if(file.getSize() > maxFileSizeBytes){
            throw new IllegalArgumentException("File size too large. Max allowed "+ maxFileSizeBytes);
        }

        //build safe store filename
        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() != null  ? file.getOriginalFilename() : "file"
        );

        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if(dotIndex >= 0){
            extension = originalFileName.substring(dotIndex).toLowerCase(); //.pdf , .jpeg , .img
        }

        String storedFileName = UUID.randomUUID().toString()+ "EMR"+ LocalDate.now() + extension;

        Path targetPath = uploadPath.resolve(storedFileName).normalize();
        if (!targetPath.startsWith(uploadPath)) {
            throw new SecurityException("Cannot store file outside upload directory: " + storedFileName);
        }

        //write to disk
        try {
            Files.copy(file.getInputStream() ,targetPath ,StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored: {} (original: {})", storedFileName, originalFileName);
            return storedFileName;
        }catch (Exception e){
            throw new RuntimeException("Failed to Store file : " + originalFileName , e);
        }
    }

    //Load a file as Spring resource
    public Resource loadFileAsResource(String storedFilename) {
        try {
            Path filePath = uploadPath.resolve(storedFilename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFound("File not found: " + storedFilename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFound("File not found: " + storedFilename);
        }
    }

    public void deleteFile(String storedFilename) {
        try {
            Path filePath = uploadPath.resolve(storedFilename).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", storedFilename);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", storedFilename, e);
            // Don't throw — DB record deletion should still proceed
        }
    }

    public String getUploadDir() {
        return uploadDir;
    }

}
