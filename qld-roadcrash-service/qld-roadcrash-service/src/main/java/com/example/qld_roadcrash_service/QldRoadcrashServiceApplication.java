package com.example.qld_roadcrash_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class QldRoadcrashServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(QldRoadcrashServiceApplication.class, args);
	}

}
