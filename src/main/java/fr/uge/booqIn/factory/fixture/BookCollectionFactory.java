package fr.uge.booqIn.factory.fixture;

import com.github.javafaker.Faker;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BookCollectionFactory {
  private static final Faker faker = new Faker();

  public static BookCollection createBookCollection() {
    return createBookCollection(new User());
  }

  public static BookCollection createBookCollection(User ownerUser) {
    String title = faker.book().title();
    String description = faker.lorem().sentence();
    boolean isPublic = faker.bool().bool();
    Date createdAt = faker.date().past(100, TimeUnit.DAYS);
    Date updatedAt = faker.date().past(100, TimeUnit.DAYS);
    return new BookCollection.Builder()
            .title(title)
            .description(description)
            .owner(ownerUser)
            .isPublic(isPublic)
            .isSmart(false)
            .collectionType(BookCollection.CollectionType.NORMAL)
            .build();
  }
}
