package net.bqc.uss.messenger.config;

import javax.sql.DataSource;

import net.bqc.uss.messenger.controller.WebhookController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
// @PropertySource only works with properties files, it sucks :3
@PropertySource("classpath:persistence.properties")
public class DataSourceConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

	@Autowired
	private Environment env;

	@Value("classpath:db/create-db.sql")
	public Resource schemaScript;

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getProperty("jdbc.driver"));
		dataSource.setUrl(env.getProperty("jdbc.url"));
		dataSource.setUsername(env.getProperty("jdbc.username"));
		dataSource.setPassword(env.getProperty("jdbc.password"));

		String execScript = env.getProperty("jdbc.execute-script");
		if ("true".equals(execScript)) {
			// add script
			ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
			populator.addScript(schemaScript);
			DatabasePopulatorUtils.execute(populator, dataSource);
		}

		return dataSource;
	}
}
