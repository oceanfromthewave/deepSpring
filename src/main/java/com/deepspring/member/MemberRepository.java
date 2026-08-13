package com.deepspring.member;

public interface MemberRepository
{
	com.deepspring.member.Member findById(Long memberId);

	void save(com.deepspring.member.Member member);
}