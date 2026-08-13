package com.deepspring.async;

import com.deepspring.transaction.TxConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TxAsyncExperiment
{
	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AsyncConfig.class, TxConfig.class);

		TxCallerService caller = context.getBean(TxCallerService.class);

		System.out.println("=== @Transactional 안에서 @Async 호출 ===");
		caller.doWorkInTransaction();

		Thread.sleep(1000);

		System.out.println();
		System.out.println("=== 대조군: 트랜잭션 없이 @Async 호출 ===");
		context.getBean(TxAsyncService.class).asyncCheck("트랜잭션 밖");

		Thread.sleep(1000);
		context.close();
	}
}
