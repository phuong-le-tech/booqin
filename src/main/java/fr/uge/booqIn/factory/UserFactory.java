package fr.uge.booqIn.factory;

import com.github.javafaker.Faker;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

public class UserFactory {

    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    public static User createUser(PasswordEncoder passwordEncoder, UserRole role) {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String email = faker.internet().emailAddress();
        String password = passwordEncoder.encode("password" + random.nextInt(1000));

        Address address = new Address(
                String.valueOf(random.nextInt(200) + 1),
                faker.address().streetName(),
                faker.address().city(),
                faker.address().zipCode()
        );

        return new User(firstName, lastName, email, password, address, role);
    }
}
