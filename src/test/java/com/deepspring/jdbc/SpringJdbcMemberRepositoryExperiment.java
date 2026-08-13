package com.deepspring.jdbc;

import com.deepspring.member.Member;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class SpringJdbcMemberRepositoryExperiment
{
	public static void main(String[] args)
	{
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl("jdbc:h2:mem:deepspring2;DB_CLOSE_DELAY=-1");
		dataSource.setUsername("sa");
		dataSource.setPassword("");

		SpringJdbcMemberRepository repository = new SpringJdbcMemberRepository(dataSource);

		System.out.println("save 전 findById(1L) = " + repository.findById(1L));

		repository.save(new Member(1L, "kim", Member.Grade.VIP));

		Member found = repository.findById(1L);
		System.out.println("save 후 findById(1L) = id=" + found.getId() + ", name=" + found.getName() + ", grade=" + found.getGrade());
	}
}
