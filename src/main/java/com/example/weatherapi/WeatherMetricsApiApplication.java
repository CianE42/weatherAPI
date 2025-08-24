package com.example.weatherapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Weather Metrics API.
 * Boots the Spring application context and starts the embedded server.
 */
@SpringBootApplication
public class WeatherMetricsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherMetricsApiApplication.class, args);
	}

}
