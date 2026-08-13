package com.deepspring.order;

public class OrderServiceTimeLoggingWrapper implements OrderServiceInterface
{
	private final OrderServiceInterface target;

	public OrderServiceTimeLoggingWrapper(OrderServiceInterface target)
	{
		this.target = target;
	}

	@Override
	public int calculatePrice(Long memberId, int price)
	{
		long start = System.currentTimeMillis();
		int result = target.calculatePrice(memberId, price);
		long end = System.currentTimeMillis();
		System.out.println("calculatePrice 실행시간 = " + (end - start) + "ms");
		return result;
	}

	@Override
	public int outerCall(Long memberId, int price)
	{
		return target.outerCall(memberId, price);
	}
}
