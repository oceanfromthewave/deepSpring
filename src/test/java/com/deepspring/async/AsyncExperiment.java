package com.deepspring.async;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AsyncExperiment
{
	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AsyncConfig.class);
		AsyncService service = context.getBean(AsyncService.class);

		System.out.println("bean class = " + service.getClass().getSimpleName());
		System.out.println("isAopProxy = " + AopUtils.isAopProxy(service));
		System.out.println("isCglibProxy = " + AopUtils.isCglibProxy(service));

		System.out.println();
		System.out.println("=== 1. 외부에서 @Async 메서드 직접 호출 ===");
		System.out.println("[" + Thread.currentThread().getName() + "] 호출 직전");
		service.asyncWork("external");
		System.out.println("[" + Thread.currentThread().getName() + "] 호출 직후 (기다렸나?)");

		Thread.sleep(1000);

		System.out.println();
		System.out.println("=== 2. Self Invocation (outCall 안에서 this.asyncWork) ===");
		service.outCall();
		System.out.println("[" + Thread.currentThread().getName() + "] outCall 반환 직후");

		Thread.sleep(1000);

		System.out.println();
		System.out.println("=== 3. 동시 5건 던져서 풀 동작 확인 (corePoolSize=2) ===");
		for (int i = 1; i <= 5; i++)
		{
			service.asyncWork("job-" + i);
		}

		Thread.sleep(2000);
		context.close();
	}
}
