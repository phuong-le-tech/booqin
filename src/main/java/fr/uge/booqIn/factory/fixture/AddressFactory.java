package fr.uge.booqIn.factory.fixture;

import com.github.javafaker.Faker;
import fr.uge.booqIn.entity.Address;

public class AddressFactory {
  private static final Faker faker = new Faker();

  public static Address createAddress() {
    return new Address(
      faker.address().streetAddress(),
      faker.address().city(),
      faker.address().country(),
      faker.address().zipCode()
    );
  }
}
