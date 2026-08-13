package com.deepspring.jdbc;

import com.deepspring.member.Member;

public class JdbcMemberRepositoryExperiment
{
	public static void main(String[] args)
	{
		JdbcMemberRepository repository = new JdbcMemberRepository();

		System.out.println("save 전 findById(1L) = " + repository.findById(1L));

		repository.save(new Member(1L, "kim", Member.Grade.VIP));

		System.out.println("save 후 findById(1L) = " + repository.findById(1L));
	}
}
