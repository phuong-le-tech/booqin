package fr.uge.booqIn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // pour tout ce qui est scheduler et co
@PropertySource(value = {"file:${user.dir}/.env", "file:${user.dir}/src/.env"}, ignoreResourceNotFound = true)
@EnableRetry
public class BooqInApplication {
	public static void main(String[] args) {
		SpringApplication.run(BooqInApplication.class, args);
	}
}
