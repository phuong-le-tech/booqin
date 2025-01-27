package fr.uge.bookin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // pour tout ce qui est scheduler et co
public class BooqInApplication {
	public static void main(String[] args) {
		SpringApplication.run(BooqInApplication.class, args);
	}

}
