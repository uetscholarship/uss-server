package net.bqc.uetscholarship.messenger;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class UetScholarshipMessengerApplication extends SpringBootServletInitializer {

	@Autowired
	private DataSource dataSource;
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(UetScholarshipMessengerApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(UetScholarshipMessengerApplication.class, args);
	}
	
	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource source = new ResourceBundleMessageSource();
		source.setBasename("i18n/messages");
		source.setDefaultEncoding("UTF-8");
		return source;
	}
	
	@Bean
	public JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(dataSource);
	}
}
