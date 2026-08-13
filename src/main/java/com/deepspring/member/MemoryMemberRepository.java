package com.deepspring.member;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Component
public class MemoryMemberRepository implements com.deepspring.member.MemberRepository
{

	private final Map<Long, com.deepspring.member.Member> store = new HashMap<>();

	public MemoryMemberRepository()
	{
		store.put(1L, new com.deepspring.member.Member(1L, "kim", Member.Grade.VIP));
		store.put(2L, new Member(2L, "lee", Member.Grade.BASIC));
	}

	@PostConstruct
	public void init()
	{
		System.out.println("MemoryMemberRepository 초기화 완료");
	}

	@Override
	public Member findById(Long memberId)
	{
		return store.get(memberId);
	}

	@Override
	public void save(Member member)
	{
		store.put(member.getId(), member);
	}
}
