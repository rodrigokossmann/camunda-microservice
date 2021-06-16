package com.course.camunda.microservicetraining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com")
public class MicroserviceTrainingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceTrainingApplication.class, args);
	}

}
