package com.example.weatherapi;

import org.springframework.boot.SpringApplication;

public class TestWeatherMetricsApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(WeatherMetricsApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
