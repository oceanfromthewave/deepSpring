package com.deepspring.integration;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Repository
public class PaymentRepository
{
	private final JdbcTemplate jdbcTemplate;

	public PaymentRepository(JdbcTemplate jdbcTemplate)
	{
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public void save(String label)
	{
		System.out.println("    [PaymentRepository.save] transaction active = " + TransactionSynchronizationManager.isActualTransactionActive());

		jdbcTemplate.queryForObject("select 1", Integer.class);
		jdbcTemplate.queryForObject("select 1", Integer.class);
	}
}
