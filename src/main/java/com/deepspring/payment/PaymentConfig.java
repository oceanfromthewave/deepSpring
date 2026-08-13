package com.deepspring.payment;

import com.deepspring.payment.adapter.RestChargeGateway;
import com.deepspring.payment.application.ChargeGateway;
import com.deepspring.payment.application.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class PaymentConfig
{
	@Bean
	public ChargeGateway chargeGateway(@Value("${payment.base-url:http://localhost:18080}") String baseUrl,
			@Value("${payment.read-timeout-millis:2000}") long readTimeoutMillis)
	{
		return new RestChargeGateway(baseUrl, Duration.ofSeconds(1), Duration.ofMillis(readTimeoutMillis));
	}

	@Bean
	public PaymentService paymentService(ChargeGateway chargeGateway)
	{
		return new PaymentService(chargeGateway);
	}
}
