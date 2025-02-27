package fr.uge.booqIn.dto.stripe;

public record PaymentIntentResponseDTO(String id, String clientSecret, String status) {
}
