package fr.uge.booqIn.dto.lending;

import java.util.List;


public record LendingResponseDTO(
        List<BookLendingShow> borrowedBooks,
        List<BookLendingShow> lentBooks,
        boolean containsAll
) {
}