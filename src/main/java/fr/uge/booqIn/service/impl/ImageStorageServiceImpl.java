package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.service.interfaces.ImageStorageServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageStorageServiceImpl implements ImageStorageServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(ImageStorageServiceImpl.class);
    
    private final Path coverImagesLocation;
    private final String apiBaseUrl;

    public ImageStorageServiceImpl(
            @Value("${app.storage.cover-images}") String coverImagesPath,
            @Value("${app.api.base-url}") String apiBaseUrl) {
        this.coverImagesLocation = Paths.get(coverImagesPath).toAbsolutePath().normalize();
        this.apiBaseUrl = apiBaseUrl;
        
        try {
            Files.createDirectories(this.coverImagesLocation);
            logger.info("Created directory for cover images: {}", this.coverImagesLocation);
        } catch (IOException ex) {
            logger.error("Could not create the directory for storing cover images", ex);
            throw new RuntimeException("Could not create the directory for storing cover images", ex);
        }
    }
    
    @Override
    public String storeCoverImage(MultipartFile file, Long bookId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }
        var originalFilename = file.getOriginalFilename();
        var fileName = UUID.randomUUID() + "_" + bookId + "_" +
                (originalFilename != null ? originalFilename : "cover.jpg");
        
        Path targetLocation = this.coverImagesLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("Stored cover image for book ID {}: {}", bookId, fileName);
        return fileName;
    }
    
    @Override
    public Resource loadCoverImageAsResource(String fileName) {
        try {
            Path filePath = this.coverImagesLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                logger.error("Cover image file not found: {}", fileName);
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            logger.error("Malformed URL when loading cover image: {}", fileName, ex);
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }
    
    @Override
    public String buildCoverImageUrl(String fileName) {
        return apiBaseUrl + "/api/v1/images/books/" + fileName;
    }
    
    @Override
    public void deleteCoverImage(String fileName) throws IOException {
        Path filePath = this.coverImagesLocation.resolve(fileName).normalize();
        boolean deleted = Files.deleteIfExists(filePath);
        
        if (deleted) {
            logger.info("Deleted cover image: {}", fileName);
        } else {
            logger.warn("Cover image not found for deletion: {}", fileName);
        }
    }
}