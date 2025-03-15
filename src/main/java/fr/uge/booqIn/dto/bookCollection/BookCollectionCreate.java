package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.user.UserShow;

import java.util.Date;
import java.util.Set;

public record BookCollectionCreate(
         String title,
         String description,
         boolean isPublic
) {}
