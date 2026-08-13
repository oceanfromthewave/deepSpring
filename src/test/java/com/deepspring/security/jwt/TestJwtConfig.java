package com.deepspring.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestJwtConfig
{
	@Bean
	public SimpleJwt simpleJwt()
	{
		JwtProperties properties = new JwtProperties();
		properties.setSecret("test-secret-key");
		properties.setExpirationMillis(60_000);
		return new SimpleJwt(properties);
	}
}
