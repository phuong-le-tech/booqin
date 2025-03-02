package fr.uge.booqIn.dto.model;

import java.util.Map;

public record SmartCollectionDtoRequest(String title, String description, boolean isPublic, boolean isSmart, String type, Map<String, Object> criteria, String owner) {
}
