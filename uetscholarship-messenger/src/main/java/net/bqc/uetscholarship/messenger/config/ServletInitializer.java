package net.bqc.uetscholarship.messenger.config;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import net.bqc.uetscholarship.messenger.UetScholarshipMessengerApplication;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(UetScholarshipMessengerApplication.class);
	}

}
