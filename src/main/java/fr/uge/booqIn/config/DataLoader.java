package fr.uge.booqIn.config;

import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.fixture.UserFactory;
import fr.uge.booqIn.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.logging.Logger;
import java.util.stream.IntStream;

@Configuration
public class DataLoader {
    private static Logger logger = Logger.getLogger(DataLoader.class.getName());
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@booqin.com").isEmpty()) {
                User admin = UserFactory.createUser(passwordEncoder, UserRole.ADMIN);
                admin.setEmail("admin@booqin.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(admin);
                logger.info("✅ Admin user created: admin@booqin.com / admin123");
                User user = UserFactory.createUser(passwordEncoder, UserRole.USER);
                user.setEmail("user@booqin.com");
                user.setPassword(passwordEncoder.encode("user123"));
                userRepository.save(user);
                logger.info("✅ User created: user@booqin.com / user123");
            } else {
                logger.info("ℹ️ Admin user already exists.");
            }
            if (userRepository.findByEmail("user@booqin.com").isEmpty()) {
                User user = UserFactory.createUser(passwordEncoder, UserRole.USER);
                user.setEmail("user@booqin.com");
                user.setPassword(passwordEncoder.encode("user123"));
                userRepository.save(user);
                logger.info("✅ User created: user@booqin.com / user123");
            } else {
                logger.info("ℹ️ User user already exists.");
            }
            if (userRepository.count() <= 1) {
                IntStream.range(0, 10).forEach(i -> {
                    User user = UserFactory.createUser(passwordEncoder, UserRole.USER);
                    userRepository.save(user);
                });
                logger.info("✅ 10 test users have been created.");
            } else {
                logger.info("ℹ️ Test users already exist.");
            }
        };
    }
}
