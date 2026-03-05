package com.company.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AssetApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssetApplication.class, args);
	}
}