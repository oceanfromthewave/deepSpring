package com.deepspring.async;

import com.deepspring.transaction.TxConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TransactionalEventExperiment
{
	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AsyncConfig.class, TxConfig.class);
		TxCallerService caller = context.getBean(TxCallerService.class);

		System.out.println("=== 1. 커밋 성공 케이스 ===");
		caller.registerSuccess("kim");

		Thread.sleep(500);

		System.out.println();
		System.out.println("=== 2. 롤백 케이스 ===");
		try
		{
			caller.registerFail("lee");
		}
		catch (RuntimeException e)
		{
			System.out.println("[main] 예외 잡음: " + e.getMessage());
		}

		Thread.sleep(500);

		System.out.println();
		System.out.println("=== 3. 트랜잭션 없이 발행 (직접 publishEvent) ===");
		context.publishEvent(new UserRegisteredEvent("park"));

		Thread.sleep(500);
		context.close();
	}
}
