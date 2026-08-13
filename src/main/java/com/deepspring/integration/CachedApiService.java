package com.deepspring.integration;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachedApiService
{
	private final ExternalApiClient externalApiClient;

	public CachedApiService(ExternalApiClient externalApiClient)
	{
		this.externalApiClient = externalApiClient;
	}

	@Cacheable("slowApi")
	public String fetch(String key)
	{
		System.out.println("[" + Thread.currentThread().getName() + "] 실제 외부 호출 발생: key=" + key);
		return externalApiClient.callWithoutTimeout();
	}

	@Cacheable(value = "slowApi", sync = true)
	public String fetchSync(String key)
	{
		System.out.println("[" + Thread.currentThread().getName() + "] 실제 외부 호출 발생(sync): key=" + key);
		return externalApiClient.callWithoutTimeout();
	}
}
