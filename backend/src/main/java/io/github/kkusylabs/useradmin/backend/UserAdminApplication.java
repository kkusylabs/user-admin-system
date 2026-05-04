package io.github.kkusylabs.useradmin.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Entry point for the User Admin backend application.
 *
 * <p>Bootstraps the Spring Boot context and starts the embedded server.
 * This class is responsible only for application startup.
 */
@SpringBootApplication
public class UserAdminApplication {

	/**
	 * Launches the User Admin application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(UserAdminApplication.class, args);
	}

}
