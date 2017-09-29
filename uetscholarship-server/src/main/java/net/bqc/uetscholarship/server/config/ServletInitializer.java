package net.bqc.uetscholarship.server.config;

import net.bqc.uetscholarship.server.UetScholarshipApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(UetScholarshipApplication.class);
	}

}
