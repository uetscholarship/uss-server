package net.bqc.uss.uetnews_server.config;

import net.bqc.uss.uetnews_server.UetNewsApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(UetNewsApplication.class);
	}

}
