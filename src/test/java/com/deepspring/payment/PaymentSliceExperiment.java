package com.deepspring.payment;

import com.deepspring.payment.application.ChargeGateway;
import com.deepspring.payment.application.PaymentService;
import com.deepspring.payment.domain.Payment;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class PaymentSliceExperiment
{
	public static void main(String[] args)
	{
		System.out.println("=== 1. Spring 없이 순수 자바로 테스트 (가짜 Gateway) ===");
		PaymentService plain = new PaymentService(payment -> true);
		Payment ok = plain.pay("order-1", 10_000);
		System.out.println("승인 성공 → status = " + ok.getStatus());

		PaymentService declined = new PaymentService(payment -> false);
		System.out.println("승인 거절 → status = " + declined.pay("order-2", 10_000).getStatus());

		PaymentService broken = new PaymentService(payment ->
		{
			throw new ChargeGateway.ChargeUnavailableException("결제사 다운", new RuntimeException());
		});
		System.out.println("통신 불가 → status = " + broken.pay("order-3", 10_000).getStatus());

		System.out.println();
		System.out.println("=== 2. 도메인 규칙 검증 ===");
		try
		{
			new Payment("order-4", 0);
		}
		catch (IllegalArgumentException e)
		{
			System.out.println("금액 0 거부: " + e.getMessage());
		}
		try
		{
			Payment p = new Payment("order-5", 100);
			p.complete();
			p.complete();
		}
		catch (IllegalStateException e)
		{
			System.out.println("중복 완료 거부: " + e.getMessage());
		}

		System.out.println();
		System.out.println("=== 3. 실제 Spring Context + HTTP (read timeout 2초, 서버 5초) ===");
		runWithSpring(2000);

		System.out.println();
		System.out.println("=== 4. 같은 구성, read timeout 10초 ===");
		runWithSpring(10000);
	}

	private static void runWithSpring(long readTimeoutMillis)
	{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.getEnvironment().getPropertySources().addFirst(
				new MapPropertySource("test", Map.of("payment.read-timeout-millis", readTimeoutMillis)));
		context.register(PaymentConfig.class);
		context.refresh();

		PaymentService service = context.getBean(PaymentService.class);

		long start = System.currentTimeMillis();
		Payment payment = service.pay("order-http", 50_000);
		System.out.println("status = " + payment.getStatus() + " (" + (System.currentTimeMillis() - start) + "ms)");

		context.close();
	}
}
