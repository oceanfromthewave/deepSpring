package com.deepspring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class PropagationServiceB
{

	private final JdbcTemplate jdbcTemplate;

	public PropagationServiceB(JdbcTemplate jdbcTemplate)
	{
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void doWorkRequired(String name, int amount)
	{
		jdbcTemplate.update("UPDATE account SET balance = balance + ? WHERE name = ?", amount, name);
		throw new RuntimeException("B 내부 장애 (REQUIRED)");
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void doWorkRequiresNew(String name, int amount)
	{
		jdbcTemplate.update("UPDATE account SET balance = balance + ? WHERE name = ?", amount, name);
		throw new RuntimeException("B 내부 장애 (REQUIRES_NEW)");
	}
}
