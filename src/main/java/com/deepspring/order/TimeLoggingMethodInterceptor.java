package com.deepspring.order;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class TimeLoggingMethodInterceptor implements MethodInterceptor
{

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		long start = System.currentTimeMillis();
		Object result = proxy.invokeSuper(obj, args); // target.method() 아니라 부모(원본) 메서드 호출
		long end = System.currentTimeMillis();
		System.out.println(method.getName() + " 실행시간 = " + (end - start) + "ms");
		return result;
	}
}
