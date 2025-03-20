package fr.uge.booqIn.entity;

import fr.uge.booqIn.factory.CriteriaStrategyFactory;
import fr.uge.booqIn.strategy.collection.CollectionCriteriaStrategy;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Entity
@DiscriminatorValue("GLOBAL")
public final class GlobalBookCollection extends AbstractBookCollection {
    private static final Logger logger = Logger.getLogger(GlobalBookCollection.class.getName());

    @Column(name = "criteria_string", columnDefinition = "TEXT")
    private String criteriaString;

    @Transient
    private CollectionCriteriaStrategy criteriaStrategy;

    @Transient
    private List<CriteriaEntry> criteria = new ArrayList<>();

    public GlobalBookCollection() {
        super();
    }

    public GlobalBookCollection(String title, String description, User owner) {
        super(title, description, owner, true);
    }

    @Override
    @Transient
    public CollectionType getCollectionType() {
        return CollectionType.GLOBAL;
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

            // Clear existing books first to avoid duplicates
            getBooks().clear();

            // Add all matching books
            matchingBooks.forEach(this::addBook);
            this.setUpdatedAt(new Date());
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

    @Override
    public void setPublic(boolean isPublic) {
        throw new UnsupportedOperationException("Global collections are always public.");
    }

    public static class Builder {
        private String title;
        private String description;
        private User owner;
        private String criteriaString = "";

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

        public Builder criteria(String criteriaString) {
            this.criteriaString = criteriaString;
            return this;
        }

        public GlobalBookCollection build() {
            GlobalBookCollection collection = new GlobalBookCollection(title, description, owner);

            if (criteriaString != null && !criteriaString.isEmpty()) {
                collection.setCriteriaString(criteriaString);
            }

            return collection;
        }
    }
}