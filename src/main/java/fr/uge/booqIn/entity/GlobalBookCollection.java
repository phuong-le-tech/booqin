package fr.uge.booqIn.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("GLOBAL")
public final class GlobalBookCollection extends AbstractBookCollection {

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

    public static class Builder {
        private String title;
        private String description;
        private User owner;

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

        public GlobalBookCollection build() {
            return new GlobalBookCollection(title, description, owner);
        }
    }

    @Override
    public void setPublic(boolean isPublic) {
        throw new UnsupportedOperationException("Global collections are always public.");
    }

}