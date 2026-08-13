package com.deepspring.order;

import org.springframework.stereotype.Component;
import com.deepspring.member.Member;

@Component
public class FixDiscountPolicy implements DiscountPolicy
{
	private static final int DISCOUNT_FIX_AMOUNT = 1000;

	@Override
	public int discount(com.deepspring.member.Member member, int price)
	{
		if(member.getGrade() == Member.Grade.VIP)
		{
			return DISCOUNT_FIX_AMOUNT;
		}
		return 0;
	}
}
