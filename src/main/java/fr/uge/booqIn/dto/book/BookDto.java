package fr.uge.booqIn.dto.book;

import fr.uge.booqIn.dto.author.AuthorDto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class BookDto {
    private Long id;
    private Set<String> isbns = new HashSet<>();
    private String title;
    private Set<AuthorDto> authors = new HashSet<>();
    private String description;
    private String publicationDate;
    private Date createdAt;
    private Date updatedAt;

    public BookDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<String> getIsbns() {
        return isbns;
    }

    public void setIsbns(Set<String> isbns) {
        this.isbns = isbns;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<AuthorDto> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<AuthorDto> authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}