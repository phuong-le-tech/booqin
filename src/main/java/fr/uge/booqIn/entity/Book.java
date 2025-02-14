    package fr.uge.booqIn.entity;

    import fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI.IndustryIdentifier;
    import jakarta.persistence.*;

    import java.util.Date;
    import java.util.List;
    import java.util.Set;

    @Entity
    public class Book {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(length = 5000)
        private String isbn10;

        @Column(length = 5000)
        private String isbn13;


        @Column(nullable = false, length = 5000)
        private String title;

        @ManyToMany(mappedBy = "books")
        private Set<Author> authors;


        @Column(nullable = false, length = 5000)
        private String description;

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "publication_date")
        private String publicationDate;

        @ManyToMany
        private List<BookCollection> collections;


        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "created_at", nullable = false, updatable = false)
        private Date createdAt = new Date();

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "updated_at")
        private Date updatedAt = new Date();

        public Book() {
        }

        public Book(String isbn10, String isbn13, String title, Set<Author> authors, String description, String publicationDate) {
            this.title = title;
            this.authors = authors;
            this.isbn10 = isbn10;
            this.isbn13 = isbn13;
            this.description = description;
            this.publicationDate = publicationDate;
        }

        public Long getId() {
            return id;
        }
        public String getIsbn10() {
            return isbn10;
        }

        public String getIsbn13() {
            return isbn13;
        }

        public String getTitle() {
            return title;
        }

        public Set<Author> getAuthors() {
            return authors;
        }

        public String getDescription() {
            return description;
        }

        public String getPublicationDate() {
            return publicationDate;
        }

        public List<BookCollection> getCollections() {
            return collections;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public Date getUpdatedAt() {
            return updatedAt;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setAuthors(Set<Author> authors) {
            this.authors = authors;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setPublicationDate(String publicationDate) {
            this.publicationDate = publicationDate;
        }

        public void setCollections(List<BookCollection> collections) {
            this.collections = collections;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public void setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
        }

        public void addCollection(BookCollection collection) {
            collections.add(collection);
            collection.getBooks().add(this);
        }

        public void removeCollection(BookCollection collection) {
            collections.remove(collection);
            collection.getBooks().remove(this);
        }

        @Override
        public String toString() {
            return "Book{" +
                    "isbn10='" + isbn10 + '\'' +
                    ", isbn13='" + isbn13 + '\'' +
                    ", title='" + title + '\'' +
                    ", authors='" + authors+ '\'' +
                    ", description='" + description + '\'' +
                    ", publicationDate='" + publicationDate + '\'' +
                    '}';
        }
    }