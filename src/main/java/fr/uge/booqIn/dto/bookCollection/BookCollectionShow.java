package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.user.UserIndex;

import java.util.Date;
import java.util.List;
import java.util.Set;

public record BookCollectionShow(
        Long id,
        String title,
        String description,
        boolean isPublic,
        List<BookIndex> books,
        UserIndex user,
        String collectionType,
        Date createdAt,
        Date updatedAt
) {
  
  public String toString() {
    return "BookCollectionShow{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", isPublic=" + isPublic +
            ", books=" + books +
            ", user=" + user +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
  }
}
