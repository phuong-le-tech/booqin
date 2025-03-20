package fr.uge.booqIn.security;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.repository.BookCollectionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("collectionSecurity")
public class CollectionSecurityExpressions {

  private final BookCollectionRepository bookCollectionRepository;

  public CollectionSecurityExpressions(BookCollectionRepository bookCollectionRepository) {
    this.bookCollectionRepository = bookCollectionRepository;
  }

  public boolean isCollectionOwner(Long collectionId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
      return false;
    }

    AbstractBookCollection collection = bookCollectionRepository.findById(collectionId).orElse(null);

    return collection != null && collection.getOwner().getId().equals(currentUser.getId());
  }

  public boolean canAccessCollection(Long collectionId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = authentication.getPrincipal() instanceof User ?
            (User) authentication.getPrincipal() : null;

    AbstractBookCollection collection = bookCollectionRepository.findById(collectionId).orElse(null);

    return collection != null && (
            collection.isPublic() ||
                    (currentUser != null && collection.getOwner().getId().equals(currentUser.getId()))
    );
  }
}