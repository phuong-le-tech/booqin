package fr.uge.booqIn.dto.book;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * DTO spécifique pour la création d'un livre via le formulaire
 */
public record BookCreateDto(
    String title,
    String author, // Format: "Prénom Nom" ou simplement "Nom"
    String description,
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date publicationDate,
    String isbn
) {
    // Constructeur par défaut nécessaire pour le binding Spring
    public BookCreateDto() {
        this(null, null, null, null, null);
    }
}