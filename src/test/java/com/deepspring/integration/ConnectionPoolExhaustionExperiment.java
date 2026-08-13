package com.deepspring.integration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.concurrent.CountDownLatch;

public class ConnectionPoolExhaustionExperiment
{
	@Configuration
	@ComponentScan(basePackages = "com.deepspring.integration")
	@EnableTransactionManagement
	static class SmallPoolConfig
	{
		@Bean
		public DataSource dataSource()
		{
			HikariDataSource ds = new HikariDataSource();
			ds.setJdbcUrl("jdbc:h2:mem:pooltest;DB_CLOSE_DELAY=-1");
			ds.setDriverClassName("org.h2.Driver");
			ds.setUsername("sa");
			ds.setMaximumPoolSize(2);        // 풀 크기 2
			ds.setConnectionTimeout(3000);   // 3초 안에 못 받으면 실패
			return ds;
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
	}

	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SmallPoolConfig.class);
		PaymentService service = context.getBean(PaymentService.class);

		boolean outside = args.length > 0 && args[0].equals("outside");
		System.out.println("=== pool size 2, 동시 3건, 외부 API 5초 | mode = "
				+ (outside ? "트랜잭션 밖" : "트랜잭션 안") + " ===");

		CountDownLatch latch = new CountDownLatch(3);
		for (int i = 1; i <= 3; i++)
		{
			final String label = "req-" + i;
			new Thread(() ->
			{
				try
				{
					if (outside)
					{
						service.payOutsideTransaction(label);
					}
					else
					{
						service.payInsideTransaction(label);
					}
				}
				catch (Exception e)
				{
					System.out.println("[" + Thread.currentThread().getName() + "] " + label
							+ " FAILED: " + e.getClass().getSimpleName());
					Throwable cause = e.getCause();
					while (cause != null)
					{
						System.out.println("    cause: " + cause.getClass().getSimpleName() + " - " + cause.getMessage());
						cause = cause.getCause();
					}
				}
				finally
				{
					latch.countDown();
				}
			}, "worker-" + i).start();
		}

		latch.await();
		context.close();
	}
}
