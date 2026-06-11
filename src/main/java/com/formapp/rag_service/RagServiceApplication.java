package com.formapp.rag_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;

@SpringBootApplication
public class RagServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(RagServiceApplication.class, args);
	}
}