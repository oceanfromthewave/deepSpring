package com.deepspring.payment.adapter;

import com.deepspring.payment.application.ChargeGateway;
import com.deepspring.payment.domain.Payment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

public class RestChargeGateway implements ChargeGateway
{
	private final RestClient restClient;

	public RestChargeGateway(String baseUrl, Duration connectTimeout, Duration readTimeout)
	{
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(connectTimeout);
		factory.setReadTimeout(readTimeout);

		this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
	}

	@Override
	public boolean charge(Payment payment)
	{
		try
		{
			String response = restClient.get().uri("/slow").retrieve().body(String.class);
			return response != null;
		}
		catch(RestClientException e)
		{
			throw new ChargeUnavailableException("결제사 통신 실패 : orderId= " + payment.getOrderId(), e);
		}
	}
}
