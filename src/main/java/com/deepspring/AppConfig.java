package com.deepspring;

import com.deepspring.member.MemberRepository;
import com.deepspring.member.MemoryMemberRepository;
import com.deepspring.order.DiscountPolicy;
import com.deepspring.order.OrderService;
import com.deepspring.order.RateDiscountPolicy;

public class AppConfig
{

	private final SimpleContainer container = new SimpleContainer();

	public MemberRepository memberRepository()
	{
		return container.getBean(MemberRepository.class, MemoryMemberRepository::new);
	}

	public DiscountPolicy discountPolicy()
	{
		return container.getBean(DiscountPolicy.class, RateDiscountPolicy::new);
	}

	public OrderService orderService()
	{
		return new OrderService(memberRepository(), discountPolicy());
	}
}
