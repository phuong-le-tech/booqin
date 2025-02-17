package fr.uge.booqIn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // pour tout ce qui est scheduler et co
@PropertySource("file:${user.dir}/src/.env")
public class BooqInApplication {
	public static void main(String[] args) {
		SpringApplication.run(BooqInApplication.class, args);
	}
}
