package com.deepspring.order;

public interface OrderServiceInterface
{
	int calculatePrice(Long memberId, int price);

	int outerCall(Long memberId, int price);
}
