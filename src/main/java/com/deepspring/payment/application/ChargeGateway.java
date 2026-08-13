package com.deepspring.payment.application;

import com.deepspring.payment.domain.Payment;

public interface ChargeGateway
{
	/**
	 * 외부 결제사에 승인 요청.
	 *
	 * @return 승인 성공 여부
	 * @throws ChargeUnavailableException 결제사와 통신 자체가 불가능한 경우
	 */
	boolean charge(Payment payment);

	class ChargeUnavailableException extends RuntimeException
	{
		public ChargeUnavailableException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
}
