package com.deepspring.integration;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService
{
	private final JdbcTemplate jdbcTemplate;
	private final ExternalApiClient externalApiClient;
	private final PaymentRepository paymentRepository;

	public PaymentService(JdbcTemplate jdbcTemplate, ExternalApiClient externalApiClient, PaymentRepository paymentRepository)
	{
		this.jdbcTemplate = jdbcTemplate;
		this.externalApiClient = externalApiClient;
		this.paymentRepository = paymentRepository;
	}

	@Transactional
	public void payInsideTransaction(String label)
	{
		long start = System.currentTimeMillis();

		jdbcTemplate.queryForObject("select 1", Integer.class);
		System.out.println("[" + Thread.currentThread().getName() + "] " + label + " DB 커넥션 확보 (+" + (System.currentTimeMillis() - start) + "ms)");

		externalApiClient.callWithoutTimeout();

		jdbcTemplate.queryForObject("select 1", Integer.class);
		System.out.println("[" + Thread.currentThread().getName() + "] " + label + " 트랜잭션 종료 (+" + (System.currentTimeMillis() - start) + "ms)");
	}

	public void payOutsideTransaction(String label)
	{
		long start = System.currentTimeMillis();

		paymentRepository.save(label);
		System.out.println("[" + Thread.currentThread().getName() + "] " + label + " 트랜잭션 종료, 커넥션 반납 (+" + (System.currentTimeMillis() - start) + "ms)");

		externalApiClient.callWithoutTimeout();
		System.out.println("[" + Thread.currentThread().getName() + "] " + label + " 외부 API 완료 (+" + (System.currentTimeMillis() - start) + "ms)");
	}

}
