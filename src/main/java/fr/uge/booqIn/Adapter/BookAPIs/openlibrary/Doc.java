package fr.uge.booqIn.Adapter.BookAPIs.openlibrary;

import java.util.List;

public record Doc(
         List<String> author_name,
         String title,
         List<String> isbn,
         Integer first_publish_year,
         String description
){}
