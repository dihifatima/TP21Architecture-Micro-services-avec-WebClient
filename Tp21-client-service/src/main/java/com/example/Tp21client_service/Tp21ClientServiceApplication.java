package com.example.Tp21client_service; // garde la ligne package de ton fichier

import com.example.Tp21client_service.entities.Client;
import com.example.Tp21client_service.repositories.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Tp21ClientServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Tp21ClientServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner initialiserBaseDeDonnees(ClientRepository clientRepository) {
		return args -> {
			if (clientRepository.count() == 0) {
				clientRepository.save(new Client(null, "Amal", 22f));
				clientRepository.save(new Client(null, "Youssef", 25f));
				clientRepository.save(new Client(null, "Salma", 30f));
			}
			clientRepository.findAll().forEach(System.out::println);
		};
	}
}