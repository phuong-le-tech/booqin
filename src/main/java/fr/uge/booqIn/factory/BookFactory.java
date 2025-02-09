package fr.uge.booqIn.factory;

import com.github.javafaker.Faker;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BookFactory {
  private static final Faker faker = new Faker();
  private static final Random random = new Random();

  public static Book createBook() {
    String title = faker.name().title();
    String author = faker.name().name();
    String isbn = faker.number().digits(10);
    String description = faker.lorem().sentence();
    String publicationDate = faker.date().past(100, TimeUnit.DAYS).toString();

    return new Book(title, author, isbn, description, publicationDate);
  }
}
