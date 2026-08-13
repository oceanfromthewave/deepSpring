package com.deepspring.order;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TimeLoggingInvocationHandler implements InvocationHandler
{
	private final Object target;

	public TimeLoggingInvocationHandler(Object target)
	{
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		long start = System.currentTimeMillis();
		Object result = method.invoke(target, args);
		long end = System.currentTimeMillis();
		System.out.println(method.getName() + " 실행시간 = " + (end - start) + "ms");
		return result;
	}
}
