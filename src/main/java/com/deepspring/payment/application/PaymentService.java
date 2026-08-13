package com.deepspring.payment.application;

import com.deepspring.payment.domain.Payment;

public class PaymentService
{
	private final ChargeGateway chargeGateway;

	public PaymentService(ChargeGateway chargeGateway)
	{
		this.chargeGateway = chargeGateway;
	}

	public Payment pay(String orderId, long amount)
	{
		Payment payment = new Payment(orderId, amount);

		try
		{
			if(chargeGateway.charge(payment))
			{
				payment.complete();
			}
			else
			{
				payment.fail();
			}
		}
		catch(ChargeGateway.ChargeUnavailableException e)
		{
			payment.fail();
		}

		return payment;
	}
}
