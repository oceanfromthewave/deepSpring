package com.deepspring.boot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"com.deepspring.boot", "com.deepspring.security"})
@ComponentScan(basePackages = {"com.deepspring.boot", "com.deepspring.validation", "com.deepspring.security", "com.deepspring.logging"})
@EntityScan(basePackages = "com.deepspring.jpa")
@EnableJpaRepositories(basePackages = "com.deepspring.jpa")
public class MinimalBootApp
{
	public static void main(String[] args)
	{
		new SpringApplicationBuilder(MinimalBootApp.class)
				.properties(
						"server.port=18080",
						"debug=true",
						"spring.datasource.url=jdbc:h2:mem:springdata;DB_CLOSE_DELAY=-1",
						"spring.datasource.driver-class-name=org.h2.Driver",
						"spring.jpa.hibernate.ddl-auto=update",
						"spring.jpa.show-sql=true")
				.run(args);
	}
}
