package fr.uge.booqIn.entity;

import fr.uge.booqIn.strategy.collection.CollectionCriteriaStrategy;
import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("SMART")
public final class SmartBookCollection extends AbstractBookCollection {

    @Transient
    private CollectionCriteriaStrategy criteriaStrategy;

    @Column(name = "criteria_json", columnDefinition = "TEXT")
    private String criteriaJson;

    public SmartBookCollection() {
        super();
    }

    public SmartBookCollection(String title, String description, User owner, boolean isPublic, CollectionCriteriaStrategy criteriaStrategy) {
        super(title, description, owner, isPublic);
        this.criteriaStrategy = criteriaStrategy;
    }

    @Override
    @Transient
    public CollectionType getCollectionType() {
        return CollectionType.SMART;
    }

    public void updateBooks(List<Book> allBooks) {
        if (criteriaStrategy != null) {
            this.setBooks(allBooks.stream()
                    .filter(criteriaStrategy::satisfy)
                    .toList());
            this.setUpdatedAt(new java.util.Date());
        }
    }

    public CollectionCriteriaStrategy getCriteriaStrategy() {
        return criteriaStrategy;
    }

    public void setCriteriaStrategy(CollectionCriteriaStrategy criteriaStrategy) {
        this.criteriaStrategy = criteriaStrategy;
    }

    public String getCriteriaJson() {
        return criteriaJson;
    }

    public void setCriteriaJson(String criteriaJson) {
        this.criteriaJson = criteriaJson;
    }

    public static class Builder {
        private String title;
        private String description;
        private User owner;
        private boolean isPublic = true;
        private CollectionCriteriaStrategy criteriaStrategy;
        private String criteriaJson;

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

        public Builder criteriaStrategy(CollectionCriteriaStrategy criteriaStrategy) {
            this.criteriaStrategy = criteriaStrategy;
            return this;
        }

        public Builder criteria(String criteriaJson) {
            this.criteriaJson = criteriaJson;
            return this;
        }

        public SmartBookCollection build() {
            SmartBookCollection collection = new SmartBookCollection(title, description, owner, isPublic, criteriaStrategy);
            collection.setCriteriaJson(criteriaJson);
            return collection;
        }
    }
}