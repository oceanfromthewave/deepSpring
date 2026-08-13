package com.deepspring.integration;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class ExternalApiClient
{
	private final RestClient noTimeOutClient;
	private final RestClient timeoutClient;

	public ExternalApiClient()
	{
		this.noTimeOutClient = RestClient.builder().baseUrl("http://localhost:18080").build();

		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(1));
		factory.setReadTimeout(Duration.ofSeconds(2));

		this.timeoutClient = RestClient.builder().baseUrl("http://localhost:18080").requestFactory(factory).build();
	}

	public String callWithoutTimeout()
	{
		return noTimeOutClient.get().uri("/slow").retrieve().body(String.class);
	}

	public String callWithTimeout()
	{
		return timeoutClient.get().uri("/slow").retrieve().body(String.class);
	}
}
