package com.civiceye.service.storage;

import com.civiceye.exception.InvalidFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 8 * 1024 * 1024; // 8 MB

    private final Path rootLocation;

    public StorageService(@Value("${civiceye.storage.upload-dir:uploads/evidence}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage directory at {}: {}", this.rootLocation, e.getMessage());
        }
    }

    public record StoredFile(String originalFilename, String contentType, long sizeBytes, String storageKey) {}

    public StoredFile storeEvidence(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Evidence file cannot be empty", 400);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File size exceeds maximum permitted limit of 8 MB", 413);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException("Only JPEG, PNG, and WebP images are allowed. Received: " + contentType, 415);
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "evidence.jpg";
        String extension = getExtension(originalFilename);
        String storageKey = "evidence/" + UUID.randomUUID() + extension;

        try {
            Path targetFile = this.rootLocation.resolve(Paths.get(storageKey).getFileName()).normalize();
            Files.createDirectories(targetFile.getParent());
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored evidence photo: original='{}', key='{}', size={} bytes", originalFilename, storageKey, file.getSize());
            return new StoredFile(originalFilename, contentType, file.getSize(), storageKey);
        } catch (IOException e) {
            log.error("Failed to store uploaded file: {}", e.getMessage());
            throw new RuntimeException("Failed to persist uploaded evidence file", e);
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot > 0 && dot < filename.length() - 1) {
            return filename.substring(dot).toLowerCase();
        }
        return ".jpg";
    }
}
