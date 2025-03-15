package fr.uge.booqIn.adapter.bookApi.openlibrary;

import java.util.List;

public record Doc(
        List<String> author_name,
        String title,
        List<String> isbn,
        Integer first_publish_year,
        String description,
        List<String> subject_facet
) {
    public List<String> getCategories() {
        return subject_facet != null ? subject_facet : List.of();
    }
}