package net.bqc.uss.uetnews_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UetNewsApplication {

	public static void main(String[] args) {
//		SpringApplication.run(UetNewsApplication.class, args);
		new SpringApplicationBuilder(UetNewsApplication.class).web(false).run(args);
	}
	
}
