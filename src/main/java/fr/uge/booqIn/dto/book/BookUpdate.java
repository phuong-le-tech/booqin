package fr.uge.booqIn.dto.book;

import fr.uge.booqIn.entity.AbstractBookCollection;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record BookUpdate(
        Optional<@Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters") String> title,

        Optional<@Size(max = 5, message = "Maximum of 5 ISBN numbers allowed")
                Set<String>> isbns,

        Optional<@Size(max = 5000, message = "Description must be less than 5000 characters") String> description,

        Optional<Date> publicationDate,

        Optional<List<AbstractBookCollection>> collections
) {
}