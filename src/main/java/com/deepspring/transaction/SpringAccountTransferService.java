package com.deepspring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class SpringAccountTransferService
{
	private final JdbcTemplate jdbcTemplate;

	public SpringAccountTransferService(JdbcTemplate jdbcTemplate)
	{
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public void transfer(String fromName, String toName, int amount)
	{
		jdbcTemplate.update("UPDATE account SET balance = balance - ? WHERE name = ?", amount, fromName);
		validate(toName);
		jdbcTemplate.update("UPDATE account SET balance = balance + ? WHERE name = ?", amount, toName);
	}

	public void validate(String toName)
	{
		if(toName.equals("broken"))
		{
			throw new IllegalStateException("이체 중간 장애 발생");
		}
	}

	public int getBalance(String name)
	{
		return jdbcTemplate.queryForObject("SELECT balance FROM account WHERE name = ?", Integer.class, name);
	}
}
