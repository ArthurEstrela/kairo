package com.skill.kairo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KairoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KairoApplication.class, args);
	}
}
