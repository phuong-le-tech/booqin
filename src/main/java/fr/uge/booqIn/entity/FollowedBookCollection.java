package fr.uge.booqIn.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.logging.Logger;

@Entity
@DiscriminatorValue("FOLLOWED")
public class FollowedBookCollection extends AbstractBookCollection {
  private static final Logger logger = Logger.getLogger(FollowedBookCollection.class.getName());
  private static final String FOLLOWED_BOOKS_COLLECTION_TITLE = "Followed Users' Books";
  private static final String FOLLOWED_BOOKS_COLLECTION_DESC = "Automatic collection of books from users that the user follows";

  public FollowedBookCollection() {
    super();
  }

  public FollowedBookCollection(User owner) {
    super(FOLLOWED_BOOKS_COLLECTION_TITLE, FOLLOWED_BOOKS_COLLECTION_DESC, owner, false);
  }

  @Override
  @Transient
  public CollectionType getCollectionType() {
    return CollectionType.FOLLOWED;
  }

  @Override
  public void setPublic(boolean isPublic) {
    throw new UnsupportedOperationException("Followed Books Collection always false.");
  }

}
