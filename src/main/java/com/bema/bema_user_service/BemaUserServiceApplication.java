package com.bema.bema_user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class BemaUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BemaUserServiceApplication.class, args);
	}

}
