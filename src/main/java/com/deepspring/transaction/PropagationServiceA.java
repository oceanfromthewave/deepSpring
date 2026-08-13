package com.deepspring.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class PropagationServiceA
{

	private final JdbcTemplate jdbcTemplate;
	private final PropagationServiceB serviceB;

	public PropagationServiceA(JdbcTemplate jdbcTemplate, PropagationServiceB serviceB)
	{
		this.jdbcTemplate = jdbcTemplate;
		this.serviceB = serviceB;
	}

	@Transactional
	public void callWithRequired(String selfName, String innerName, int amount)
	{
		jdbcTemplate.update("UPDATE account SET balance = balance + ? WHERE name = ?", amount, selfName);

		try
		{
			serviceB.doWorkRequired(innerName, amount);
		}
		catch(RuntimeException e)
		{
			System.out.println("  A가 예외 잡음: " + e.getMessage());
		}
	}

	@Transactional
	public void callWithRequiresNew(String selfName, String innerName, int amount)
	{
		jdbcTemplate.update("UPDATE account SET balance = balance + ? WHERE name = ?", amount, selfName);

		try
		{
			serviceB.doWorkRequiresNew(innerName, amount);
		}
		catch(RuntimeException e)
		{
			System.out.println("  A가 예외 잡음: " + e.getMessage());
		}
	}
}
