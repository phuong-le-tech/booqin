package fr.uge.booqIn.factory.fixture;

import com.github.javafaker.Faker;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BookCollectionFactory {
  private static final Faker faker = new Faker();

  public static BookCollection createBookCollection() {
    String title = faker.book().title();
    String description = faker.lorem().sentence();
    User owner = new User();  // à changer par un user qui existe ??
    boolean isPublic = faker.bool().bool();
    Date createdAt = faker.date().past(100, TimeUnit.DAYS);
    Date updatedAt = faker.date().past(100, TimeUnit.DAYS);
    return new BookCollection.Builder()
            .title(title)
            .description(description)
            .owner(owner)
            .isPublic(isPublic)
            .isSmart(false)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .collectionType(BookCollection.CollectionType.NORMAL)
            .build();
  }
}
