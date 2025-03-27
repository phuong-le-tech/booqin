package fr.uge.booqIn.dto.notif;

import java.time.LocalDateTime;

public record NotificationDTO(
    Long id,
    String message,
    String type,
    LocalDateTime createdAt,
    boolean read
) {}