package fr.uge.booqIn.adapter.bookApi.googleApi;

import java.util.List;

public record VolumeInfo(
        String title,
        List<String> authors,
        String description,
        String publishedDate,
        List<IndustryIdentifier> industryIdentifiers,
        List<String> categories
) {
}