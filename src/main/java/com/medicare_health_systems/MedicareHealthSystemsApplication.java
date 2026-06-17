package com.medicare_health_systems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MedicareHealthSystemsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicareHealthSystemsApplication.class, args);
	}

}
