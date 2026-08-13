package com.deepspring.order;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;
import com.deepspring.member.Member;

@Component
@Primary
public class RateDiscountPolicy implements DiscountPolicy
{
	private static final int DISCOUNT_PERCENT = 10;

	@Override
	public int discount(Member member, int price)
	{
		if(member.getGrade() == Member.Grade.VIP)
		{
			return price * DISCOUNT_PERCENT / 100;
		}
		return 0;
	}
}
