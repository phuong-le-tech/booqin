package fr.uge.booqIn.entity;

import fr.uge.booqIn.factory.CriteriaStrategyFactory;
import fr.uge.booqIn.strategy.collection.CollectionCriteriaStrategy;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Entity
@DiscriminatorValue("SMART")
public final class SmartBookCollection extends AbstractBookCollection {
  private static final Logger logger = Logger.getLogger(SmartBookCollection.class.getName());
  @Column(name = "criteria_string", columnDefinition = "TEXT")
  private String criteriaString;

  @Transient
  private CollectionCriteriaStrategy criteriaStrategy;

  @Transient
  private List<CriteriaEntry> criteria = new ArrayList<>();

  public SmartBookCollection() {
    super();
  }

  public SmartBookCollection(String title, String description, User owner, boolean isPublic) {
    super(title, description, owner, isPublic);
  }

  @Override
  @Transient
  public CollectionType getCollectionType() {
    return CollectionType.SMART;
  }

  /**
   * Update the books in this collection based on the criteria strategy
   *
   * @param allBooks All books to be filtered according to the criteria
   */
  public void updateBooks(List<Book> allBooks) {
    if (criteriaStrategy != null) {
      List<Book> matchingBooks = allBooks.stream()
              .filter(criteriaStrategy::satisfy)
              .toList();

      logger.info("Found " + matchingBooks.size() + " books matching the criteria");
      matchingBooks.forEach(this::addBook);
      this.setUpdatedAt(LocalDateTime.now());
    }
  }

  /**
   * Initialize the criteria strategy from the persisted criteria string
   *
   * @param criteriaFactory The factory to create the strategy
   */
  public void initializeCriteriaStrategy(CriteriaStrategyFactory criteriaFactory) {
    if (criteriaFactory != null && criteriaString != null && !criteriaString.isEmpty()) {
      this.criteriaStrategy = criteriaFactory.createCriteriaStrategy(criteriaString);
      this.criteria = criteriaFactory.parseCriteriaString(criteriaString);
    }
    logger.info("Initialized criteria strategy: " + criteriaStrategy);
    logger.info("Parsed criteria: " + criteria);
  }

  public CollectionCriteriaStrategy getCriteriaStrategy() {
    return criteriaStrategy;
  }

  public void setCriteriaStrategy(CollectionCriteriaStrategy criteriaStrategy) {
    this.criteriaStrategy = criteriaStrategy;
  }

  public String getCriteriaString() {
    return criteriaString;
  }

  public void setCriteriaString(String criteriaString) {
    this.criteriaString = criteriaString;
  }

  /**
   * Get the current criteria entries parsed from the criteria string
   *
   * @return List of criteria entries
   */
  public List<CriteriaEntry> getCriteria() {
    return criteria;
  }

  /**
   * Add a new criteria entry and update the criteria string
   *
   * @param key   Criteria key
   * @param value Criteria value
   */
  public void addCriteria(String key, String value, CriteriaStrategyFactory factory) {
    Objects.requireNonNull(factory, "Factory must not be null");

    criteria.add(new CriteriaEntry(key, value));
    updateCriteriaString();

    initializeCriteriaStrategy(factory);
  }

  /**
   * Remove criteria by key and update the criteria string
   *
   * @param key Criteria key to remove
   */
  public void removeCriteria(String key, CriteriaStrategyFactory factory) {
    Objects.requireNonNull(factory, "Factory must not be null");

    criteria.removeIf(entry -> entry.key().equals(key));
    updateCriteriaString();

    initializeCriteriaStrategy(factory);

  }

  /**
   * Update the criteria string based on the criteria list
   */
  private void updateCriteriaString() {
    if (criteria.isEmpty()) {
      this.criteriaString = "";
      return;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < criteria.size(); i++) {
      CriteriaEntry entry = criteria.get(i);
      sb.append(entry.key()).append(":").append(entry.value());

      if (i < criteria.size() - 1) {
        sb.append(";");
      }
    }

    this.criteriaString = sb.toString();
  }

  public static class Builder {
    private String title;
    private String description;
    private User owner;
    private boolean isPublic = true;
    private String criteriaString;

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder owner(User owner) {
      this.owner = owner;
      return this;
    }

    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    public Builder criteria(String criteriaString) {
      this.criteriaString = criteriaString;
      return this;
    }

    public SmartBookCollection build() {
      SmartBookCollection collection = new SmartBookCollection(title, description, owner, isPublic);

      if (!criteriaString.isEmpty()) {
        collection.setCriteriaString(criteriaString);
      }

      return collection;
    }
  }
}