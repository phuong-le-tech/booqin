package fr.uge.booqIn.entity;

import fr.uge.booqIn.model.BookCollection;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.logging.Logger;

@Entity
@DiscriminatorValue("AUTHOR")
public class AuthorBookCollection extends AbstractBookCollection {
  private static final Logger logger = Logger.getLogger(AuthorBookCollection.class.getName());
  private static final String AUTHOR_BOOKS_COLLECTION_TITLE = "Books by ";
  private static final String AUTHOR_BOOKS_COLLECTION_DESC = "Collection of all books by ";

  @Column(name = "author_name", columnDefinition = "VARCHAR(255)")
  private String authorName;

  public AuthorBookCollection() {
    super();
  }

  public AuthorBookCollection(User owner, String authorName) {
    super(AUTHOR_BOOKS_COLLECTION_TITLE + authorName, AUTHOR_BOOKS_COLLECTION_DESC + authorName, owner, true);
    this.authorName = authorName;
  }

  public String getAuthorName() {
    return authorName;
  }

  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  @Override
  @Transient
  public BookCollection.CollectionType getCollectionType() {
    return BookCollection.CollectionType.AUTHOR;
  }

  @Override
  public void setPublic(boolean isPublic) {
    throw new UnsupportedOperationException("Author Books Collection always public.");
  }
}