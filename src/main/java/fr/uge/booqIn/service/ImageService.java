package fr.uge.booqIn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final String uploadDir;

    private static final int THUMBNAIL_WIDTH = 150;
    private static final int THUMBNAIL_HEIGHT = 225;

    public ImageService(@Value("${file.upload-dir}") String uploadDir,
                        @Value("${app.image-base-url}") String imageBaseUrl) {
        this.uploadDir = uploadDir;
        logger.info("ImageService initialized with uploadDir: {}, imageBaseUrl: {}", uploadDir, imageBaseUrl);

        try {
            Files.createDirectories(Paths.get(uploadDir, "books", "covers"));
            Files.createDirectories(Paths.get(uploadDir, "books", "thumbnails"));
            logger.info("Created image directories in: {}", uploadDir);
        } catch (IOException e) {
            logger.error("Failed to create image directories: {}", e.getMessage(), e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }

    public String saveBookCover(MultipartFile file, Long bookId) throws IOException {
        logger.info("Saving book cover for book ID: {}, original filename: {}", bookId, file.getOriginalFilename());

        if (file.isEmpty()) {
            logger.error("Cannot save empty file for book ID: {}", bookId);
            throw new IllegalArgumentException("File is empty");
        }

        var originalFilename = file.getOriginalFilename();
        var extension = getFileExtension(originalFilename);
        var fileName = bookId + "_" + UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        var targetDir = Paths.get(uploadDir, "books", "covers").toAbsolutePath().normalize();
        Files.createDirectories(targetDir);
        var targetPath = targetDir.resolve(fileName);

        logger.debug("Saving file to: {}", targetPath);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("File saved successfully: {}", targetPath);

        try {
            var thumbnailPath = createThumbnail(targetPath, bookId);
            logger.info("Thumbnail created: {}", thumbnailPath);
        } catch (Exception e) {
            logger.error("Failed to create thumbnail for book ID: {}, error: {}", bookId, e.getMessage(), e);
        }
        return "books/covers/" + fileName;
    }

    public String createThumbnail(Path originalImagePath, Long bookId) throws IOException {
        logger.debug("Creating thumbnail for book ID: {} from path: {}", bookId, originalImagePath);

        var originalFilename = originalImagePath.getFileName().toString();
        var extension = getFileExtension(originalFilename);
        var thumbFileName = "thumb_" + bookId + "_" + UUID.randomUUID() +
                (extension.isEmpty() ? "" : "." + extension);

        var thumbnailDir = Paths.get(uploadDir, "books", "thumbnails").toAbsolutePath().normalize();
        Files.createDirectories(thumbnailDir);
        var thumbnailPath = thumbnailDir.resolve(thumbFileName);

        logger.debug("Thumbnail will be saved to: {}", thumbnailPath);

        try (InputStream is = Files.newInputStream(originalImagePath)) {
            BufferedImage originalImage = ImageIO.read(is); // Peut etre utiliser librairie externe ici

            if (originalImage == null) {
                logger.error("Failed to read image file: {}", originalImagePath);
                throw new IOException("Cannot read image file, format may be unsupported");
            }

            var originalWidth = originalImage.getWidth();
            var originalHeight = originalImage.getHeight();

            logger.debug("Original image dimensions: {}x{}", originalWidth, originalHeight);

            var aspectRatio = (double) originalWidth / originalHeight;
            int targetWidth, targetHeight;

            if (originalWidth > originalHeight) {
                targetWidth = THUMBNAIL_WIDTH;
                targetHeight = (int) (THUMBNAIL_WIDTH / aspectRatio);
            } else {
                targetHeight = THUMBNAIL_HEIGHT;
                targetWidth = (int) (THUMBNAIL_HEIGHT * aspectRatio);
            }

            logger.debug("Thumbnail dimensions: {}x{}", targetWidth, targetHeight);

            var thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            var g = thumbnail.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g.dispose();

            var success = ImageIO.write(thumbnail, extension, thumbnailPath.toFile());
            if (!success) {
                logger.warn("No appropriate writer found for extension: {}", extension);
                success = ImageIO.write(thumbnail, "jpg", thumbnailPath.toFile());
                if (!success) {
                    throw new IOException("Could not save thumbnail in any supported format");
                }
            }
            logger.info("Thumbnail created successfully");
        }
        return "books/thumbnails/" + thumbFileName;
    }

    public String downloadBookCoverFromUrl(String imageUrl, Long bookId) {
        logger.info("Downloading book cover from URL: {} for book ID: {}", imageUrl, bookId);

        try {
            if (imageUrl.startsWith("http:")) {
                imageUrl = "https:" + imageUrl.substring(5);
                logger.debug("URL converted to HTTPS: {}", imageUrl);
            }

            var url = URI.create(imageUrl).toURL();
            var connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            logger.debug("Opening connection to URL: {}", url);

            var responseCode = connection.getResponseCode();
            logger.debug("Response code: {}", responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.error("Failed to download image: HTTP error code {}", responseCode);
                throw new IOException("Failed to download image: HTTP error code " + responseCode);
            }

            var contentType = connection.getContentType();
            logger.debug("Content type: {}", contentType);

            var path = url.getPath();
            var extension = getFileExtension(path);

            if (extension.isEmpty() && contentType != null) {
                if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                    extension = "jpg";
                } else if (contentType.contains("png")) {
                    extension = "png";
                } else if (contentType.contains("gif")) {
                    extension = "gif";
                } else {
                    extension = "jpg"; // Par défaut
                }
                logger.debug("Determined extension from content type: {}", extension);
            } else if (extension.isEmpty()) {
                extension = "jpg"; // Par défaut si on ne peut pas déterminer
                logger.debug("Using default extension: jpg");
            }
            var fileName = bookId + "_" + UUID.randomUUID() + "." + extension;
            var targetDir = Paths.get(uploadDir, "books", "covers").toAbsolutePath().normalize();
            Files.createDirectories(targetDir);
            var targetPath = targetDir.resolve(fileName);

            logger.debug("Will save downloaded image to: {}", targetPath);

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Image downloaded successfully to: {}", targetPath);

                if (!Files.exists(targetPath)) {
                    logger.error("File does not exist after download: {}", targetPath);
                    throw new IOException("File does not exist after download");
                }

                long fileSize = Files.size(targetPath);
                logger.debug("Downloaded file size: {} bytes", fileSize);

                if (fileSize == 0) {
                    logger.error("Downloaded file is empty: {}", targetPath);
                    throw new IOException("Downloaded file is empty");
                }
            }

            try {
                var thumbnailPath = createThumbnail(targetPath, bookId);
                logger.info("Thumbnail created for downloaded image: {}", thumbnailPath);
            } catch (Exception e) {
                logger.error("Failed to create thumbnail for downloaded image: {}", e.getMessage(), e);
                // Continuer même si la création de la miniature échoue
            }

            return "books/covers/" + fileName;
        } catch (IOException e) {
            logger.error("Failed to download image from URL: {}, error: {}", imageUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to download image from URL: " + e.getMessage(), e);
        }
    }
}