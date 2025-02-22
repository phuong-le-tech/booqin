package fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI;

import java.util.List;

public record VolumeInfo(String title, List<String> authors, String publishedDate, String description, List<IndustryIdentifier> industryIdentifiers) {
}