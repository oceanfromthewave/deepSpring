package com.deepspring.integration;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * payOutsideTransaction 안의 this.saveInTransaction() 호출이
 * 실제로 트랜잭션을 여는지 확인.
 */
public class SelfInvocationCheckExperiment
{
	public static void main(String[] args)
	{
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(ConnectionPoolExhaustionExperiment.SmallPoolConfig.class);

		PaymentService service = context.getBean(PaymentService.class);
		System.out.println("bean = " + service.getClass().getSimpleName());
		System.out.println("isCglibProxy = " + AopUtils.isCglibProxy(service));

		System.out.println();
		System.out.println("=== Bean 분리 후: PaymentService -> PaymentRepository ===");
		service.payOutsideTransaction("separated");

		context.close();
	}
}
