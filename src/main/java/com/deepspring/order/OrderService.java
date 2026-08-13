package com.deepspring.order;

import com.deepspring.member.Member;
import com.deepspring.member.MemberRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OrderService implements OrderServiceInterface
{
	private final MemberRepository memberRepository;
	private final DiscountPolicy discountPolicy;

	public OrderService(MemberRepository memberRepository, @Qualifier("fixDiscountPolicy") DiscountPolicy discountPolicy)
	{
		this.memberRepository = memberRepository;
		this.discountPolicy = discountPolicy;
	}

	public int calculatePrice(Long memberId, int price)
	{
		Member member = memberRepository.findById(memberId);
		int discountAmount = discountPolicy.discount(member, price);
		return price - discountAmount;
	}

	@Override
	public int outerCall(Long memberId, int price)
	{
		System.out.println("outerCall 진입");
		return calculatePrice(memberId, price);
	}
}
