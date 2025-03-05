package fr.uge.booqIn.dto.book;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DTO spécifique pour la création d'un livre via le formulaire
 */
public class BookCreateDto {
    private String title;
    private List<String> authors = new ArrayList<>(); // Format: "Prénom Nom" ou simplement "Nom"
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date publicationDate;

    private List<String> isbn = new ArrayList<>();

    // Constructeur par défaut nécessaire pour le binding Spring
    public BookCreateDto() {
    }

    // Constructeur complet
    public BookCreateDto(String title, List<String> authors, String description, Date publicationDate, List<String> isbn) {
        this.title = title;
        this.authors = authors != null ? authors : new ArrayList<>();
        this.description = description;
        this.publicationDate = publicationDate;
        this.isbn = isbn != null ? isbn : new ArrayList<>();
    }

    // Getters et setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public List<String> getIsbn() {
        return isbn;
    }

    public void setIsbn(List<String> isbn) {
        this.isbn = isbn;
    }
}