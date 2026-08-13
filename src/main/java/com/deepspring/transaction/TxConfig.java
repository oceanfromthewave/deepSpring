package com.deepspring.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class TxConfig
{
	@Bean
	public DataSource dataSource()
	{
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).addScript("schema.sql").build();
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource)
	{
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public PlatformTransactionManager transactionManager(DataSource dataSource)
	{
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public SpringAccountTransferService springAccountTransferService(JdbcTemplate jdbcTemplate)
	{
		return new SpringAccountTransferService(jdbcTemplate);
	}

	@Bean
	public PropagationServiceB propagationServiceB(JdbcTemplate jdbcTemplate)
	{
		return new PropagationServiceB(jdbcTemplate);
	}

	@Bean
	public PropagationServiceA propagationServiceA(JdbcTemplate jdbcTemplate, PropagationServiceB propagationServiceB)
	{
		return new PropagationServiceA(jdbcTemplate, propagationServiceB);
	}

}
