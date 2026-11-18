package com.example.service_car;

import com.example.service_car.entities.Car;
import com.example.service_car.repositories.CarRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServiceCarApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceCarApplication.class, args);
	}
	@Bean
    CommandLineRunner initialiserBaseH2(CarRepository voitureRepository) {
		return args -> {
			// (id, brand, model, matricule, client_id)
			voitureRepository.save(new Car(null,  "Corolla", "A 25 333",  1L));
			voitureRepository.save(new Car(null, "Megane",  "B 6 3456",  1L));
			voitureRepository.save(new Car(null,  "301",     "A 55 4444", 2L));

			voitureRepository.findAll().forEach(System.out::println);
		};
	}
}
