package com.deepspring.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService
{
	@Async
	public void asyncWork(String label)
	{
		System.out.println("[" + Thread.currentThread().getName() + "] asyncWOrk 시작: " + label);
		sleep(500);
		System.out.println("[" + Thread.currentThread().getName() + "] asyncWork 끝: " + label);
	}

	public void outCall()
	{
		System.out.println("[" + Thread.currentThread().getName() + "] outCall 시작");
		asyncWork("self-invocation");
		System.out.println("[" + Thread.currentThread().getName() + "] outCall 끝");
	}

	private void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
}
