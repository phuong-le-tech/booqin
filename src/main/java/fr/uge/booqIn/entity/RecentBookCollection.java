package fr.uge.booqIn.entity;

import fr.uge.booqIn.interfaces.BookCollection;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.logging.Logger;

@Entity
@DiscriminatorValue("RECENT")
public class RecentBookCollection extends AbstractBookCollection {
  private static final Logger logger = Logger.getLogger(RecentBookCollection.class.getName());
  private static final String RECENT_BOOKS_COLLECTION_TITLE = "Recent Books";
  private static final String RECENT_BOOKS_COLLECTION_DESC = "Books added in the last 30 days";
  private static final int DEFAULT_DAYS_RANGE = 30;
  @Column(name = "days_range", columnDefinition = "integer default 30")
  private int daysRange = DEFAULT_DAYS_RANGE;

  public RecentBookCollection() {
    super();
  }

  public RecentBookCollection(User owner) {
    super(RECENT_BOOKS_COLLECTION_TITLE, RECENT_BOOKS_COLLECTION_DESC, owner, true);
  }

  @Override
  @Transient
  public BookCollection.CollectionType getCollectionType() {
    return BookCollection.CollectionType.RECENT;
  }

  @Override
  public void setPublic(boolean isPublic) {
    throw new UnsupportedOperationException("Recent Books Collection always public.");
  }

  public int getDaysRange() {
    return daysRange;
  }

  public void setDaysRange(int daysRange) {
    this.daysRange = daysRange;
  }
}
