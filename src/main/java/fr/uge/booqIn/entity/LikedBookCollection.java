package fr.uge.booqIn.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.logging.Logger;

@Entity
@DiscriminatorValue("LIKED")
public class LikedBookCollection extends AbstractBookCollection {
  private static final Logger logger = Logger.getLogger(LikedBookCollection.class.getName());
  private static final String FOLLOWED_BOOKS_COLLECTION_TITLE = "Liked Books";
  private static final String FOLLOWED_BOOKS_COLLECTION_DESC = "Automatic collection of books that the user has liked";

  public LikedBookCollection() {
    super();
  }

  public LikedBookCollection(User owner, boolean isPublic) {
    super(FOLLOWED_BOOKS_COLLECTION_TITLE, FOLLOWED_BOOKS_COLLECTION_DESC, owner, isPublic);
  }

  @Override
  @Transient
  public CollectionType getCollectionType() {
    return CollectionType.LIKED;
  }


}
