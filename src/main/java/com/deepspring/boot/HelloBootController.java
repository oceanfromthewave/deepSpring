package com.deepspring.boot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloBootController
{
	private final AppProps appProps;

	public HelloBootController(AppProps appProps)
	{
		this.appProps = appProps;
	}

	@GetMapping("/hello")
	public String hello()
	{
		return "hello from " + appProps.getName() + " v" + appProps.getVersion() + " (admin=" + appProps.getAdmin()
				.getEmail() + ", maxUsers=" + appProps.getAdmin().getMaxUsers() + ")";
	}

	@GetMapping("/admin/hello")
	public String adminHello()
	{
		return "admin only hello";
	}

	@GetMapping("/slow")
	public String slow() throws InterruptedException
	{
		System.out.println("[" + Thread.currentThread().getName() + "] /slow 시작");
		Thread.sleep(5000);
		System.out.println("[" + Thread.currentThread().getName() + "] /slow 응답");
		return "slow response";
	}
}
