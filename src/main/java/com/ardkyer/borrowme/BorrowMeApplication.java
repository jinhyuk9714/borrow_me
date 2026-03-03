package com.ardkyer.borrowme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
public class BorrowMeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BorrowMeApplication.class, args);
	}

}
