package net.bqc.uetscholarship.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UetScholarshipApplication {

	public static void main(String[] args) {
		SpringApplication.run(UetScholarshipApplication.class, args);
	}
	
}
