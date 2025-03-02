package fr.uge.booqIn.dto.auth;

public record UserMeDto(long id, String firstName, String lastName, String email, String role) {
}
