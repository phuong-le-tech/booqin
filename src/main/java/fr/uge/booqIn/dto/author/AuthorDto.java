package fr.uge.booqIn.dto.author;

public record AuthorDto(Long id, String firstName, String lastName) {
    public String getFullName() {
        return firstName + " " + lastName;
    }
}