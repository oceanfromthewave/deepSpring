package com.deepspring.payment.domain;

public class Payment
{
	public enum Status
	{
		REQUESTED, COMPLETED, FAILED
	}

	private final String orderId;
	private final long amount;
	private Status status;

	public Payment(String orderId, long amount)
	{
		if(orderId == null || orderId.isBlank())
		{
			throw new IllegalArgumentException("orderId는 필수입니다");
		}
		if(amount <= 0)
		{
			throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다: " + amount);
		}

		this.orderId = orderId;
		this.amount = amount;
		this.status = Status.REQUESTED;
	}

	public void complete()
	{
		if(status != Status.REQUESTED)
		{
			throw new IllegalStateException("요청 상태에서만 완료할 수 있습니다: " + status);
		}
		this.status = Status.COMPLETED;
	}

	public void fail()
	{
		if(status != Status.REQUESTED)
		{
			throw new IllegalStateException("요청 상태에서만 실패 처리할 수 있습니다: " + status);
		}
		this.status = Status.FAILED;
	}

	public String getOrderId()
	{
		return orderId;
	}

	public long getAmount()
	{
		return amount;
	}

	public Status getStatus()
	{
		return status;
	}
}
