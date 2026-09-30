package com.ecommerce.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {

		// Force application timezone to UTC
		TimeZone.setDefault(
				TimeZone.getTimeZone("UTC")
		);

		SpringApplication.run(
				NotificationServiceApplication.class,
				args
		);
	}
}