package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.ImageStorageServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    
    private final ImageStorageServiceInterface imageStorageService;
    private final BookServiceInterface bookService;

    public ImageController(
            ImageStorageServiceInterface imageStorageService, 
            BookServiceInterface bookService) {
        this.imageStorageService = imageStorageService;
        this.bookService = bookService;
    }

    @GetMapping("/books/{fileName:.+}")
    public ResponseEntity<Resource> getCoverImage(@PathVariable String fileName) {
        logger.info("🖼️ Récupération de l'image de couverture: {}", fileName);
        
        var file = imageStorageService.loadCoverImageAsResource(fileName);

        var contentType = determineContentType(fileName);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, must-revalidate")
                .body(file);
    }

    @PostMapping("/books/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadCoverImage(
            @PathVariable Long bookId,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        logger.info("🔄 Upload d'une nouvelle image de couverture pour le livre ID: {}", bookId);

        var book = bookService.findBookById(bookId);

        if (book.getCoverImagePath() != null && !Boolean.TRUE.equals(book.getIsExternalImage())) {
            try {
                imageStorageService.deleteCoverImage(book.getCoverImagePath());
            } catch (IOException e) {
                logger.warn("Impossible de supprimer l'ancienne image: {}", book.getCoverImagePath(), e);
            }
        }

        var fileName = imageStorageService.storeCoverImage(file, bookId);

        book.setCoverImagePath(fileName);
        book.setIsExternalImage(false);
        bookService.updateBook(book);
        
        var imageUrl = imageStorageService.buildCoverImageUrl(fileName);
        return ResponseEntity.ok(
                ApiResponse.success("Cover image uploaded successfully", imageUrl));
    }
    
    private String determineContentType(String fileName) {
        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (fileName.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.toLowerCase().endsWith(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
        }
    }
}