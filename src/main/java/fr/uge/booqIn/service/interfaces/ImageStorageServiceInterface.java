package fr.uge.booqIn.service.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageStorageServiceInterface {

    /**
     * Store a book cover image
     *
     * @param file The image file to store
     * @param bookId The ID of the book to associate the image with
     * @return The filename of the stored image
     * @throws IOException if storage operation fails
     */
    String storeCoverImage(MultipartFile file, Long bookId) throws IOException;
    
    /**
     * Load a cover image as a Resource
     *
     * @param fileName The filename of the image to load
     * @return The image resource
     * @throws RuntimeException if the file doesn't exist or can't be loaded
     */
    Resource loadCoverImageAsResource(String fileName);
    
    /**
     * Build a complete URL for a cover image
     *
     * @param fileName The filename of the image
     * @return The complete URL to access the image
     */
    String buildCoverImageUrl(String fileName);
    
    /**
     * Delete a cover image
     *
     * @param fileName The filename of the image to delete
     * @throws IOException if deletion operation fails
     */
    void deleteCoverImage(String fileName) throws IOException;
}