package fr.uge.booqIn.factory.fixture;

import com.github.javafaker.Faker;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Author;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BookFactory {
  private static final Faker faker = new Faker();

  public static Book createBook() {
    String title = faker.book().title();
    String isbn = faker.number().digits(10);
    String description = faker.lorem().sentence();
    Date publicationDate = faker.date().past(100, TimeUnit.DAYS);

    // Création d'un auteur avec prénom et nom générés par Faker
    String firstName = faker.name().firstName();
    String lastName = faker.name().lastName();
    Author author = new Author(firstName, lastName);

    Set<Author> authors = new HashSet<>();
    authors.add(author);

    // Construction du Book via son Builder
    return new Book.Builder()
            .title(title)
            .authors(authors)
            .isbns(Collections.singleton(isbn))
            .description(description)
            .publicationDate(publicationDate)
            .build();
  }
}
