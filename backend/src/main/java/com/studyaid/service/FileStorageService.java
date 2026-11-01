package com.studyaid.service;

import com.studyaid.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.storage.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
            Files.createDirectories(this.fileStorageLocation.resolve("avatars"));
            Files.createDirectories(this.fileStorageLocation.resolve("materials"));
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    public String storeFile(MultipartFile file, String subfolder, UUID userId) {
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedFileName = userId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(subfolder).resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + subfolder + "/" + storedFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String subfolder, String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(subfolder).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new BadRequestException("File path is invalid: " + fileName);
        }
    }

    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) return false;
        try {
            String relative = fileUrl.replaceFirst("^/uploads/", "");
            Path filePath = this.fileStorageLocation.resolve(relative).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}

