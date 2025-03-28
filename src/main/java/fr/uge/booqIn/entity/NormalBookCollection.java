package fr.uge.booqIn.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("NORMAL")
public final class NormalBookCollection extends AbstractBookCollection {

    public NormalBookCollection() {
        super();
    }

    public NormalBookCollection(String title, String description, boolean isPublic, User owner) {
        super(title, description, owner, isPublic);
    }

    @Override
    @Transient
    public CollectionType getCollectionType() {
        return CollectionType.NORMAL;
    }

    public static class Builder {
        private String title;
        private String description;
        private User owner;
        private boolean isPublic = false;

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

        public NormalBookCollection build() {
          return new NormalBookCollection(title, description, isPublic, owner);
        }
    }
}