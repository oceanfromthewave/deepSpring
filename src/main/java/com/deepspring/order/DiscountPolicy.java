package com.deepspring.order;

import com.deepspring.member.Member;

public interface DiscountPolicy
{
	int discount(Member member, int price);
}
