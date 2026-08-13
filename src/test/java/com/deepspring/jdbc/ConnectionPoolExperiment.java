package com.deepspring.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;

public class ConnectionPoolExperiment
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("=== DriverManagerDataSource (Pool 없음) ===");
		DriverManagerDataSource noPool = new DriverManagerDataSource();
		noPool.setUrl("jdbc:h2:mem:pooltest1;DB_CLOSE_DELAY=-1");
		noPool.setUsername("sa");
		noPool.setPassword("");
		printConnectionIdentityThreeTimes(noPool);

		System.out.println();
		System.out.println("=== HikariCP (Pool 있음, maximumPoolSize=1) ===");
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:h2:mem:pooltest2;DB_CLOSE_DELAY=-1");
		config.setUsername("sa");
		config.setPassword("");
		config.setMaximumPoolSize(1);
		HikariDataSource pooled = new HikariDataSource(config);
		printConnectionIdentityThreeTimes(pooled);
		pooled.close();
	}

	private static void printConnectionIdentityThreeTimes(DataSource dataSource) throws Exception
	{
		for (int i = 1; i <= 3; i++)
		{
			try (Connection conn = dataSource.getConnection())
			{
				Connection real = conn.unwrap(Connection.class);
				System.out.println(i + "번째 getConnection() 실제 Connection identity = " + System.identityHashCode(real));
			}
		}
	}
}
