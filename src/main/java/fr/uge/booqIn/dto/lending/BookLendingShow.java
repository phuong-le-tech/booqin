package fr.uge.booqIn.dto.lending;

import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.enums.BookStatus;

import java.time.LocalDateTime;

public record BookLendingShow(
        Long id,
        BookShow book,
        UserIndex borrower,
        UserIndex lender,
        BookStatus status,
        LocalDateTime reservedAt,
        LocalDateTime startDate,
        LocalDateTime dueDate,
        LocalDateTime returnDate
) {
}