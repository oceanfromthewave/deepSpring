package com.deepspring.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionInvocationHandler implements InvocationHandler
{

	private final Object target;

	public TransactionInvocationHandler(Object target)
	{
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		FakeConnection conn = new FakeConnection();
		TransactionSynchronizationManager.bindConnection(conn);

		try
		{
			Object result = method.invoke(target, args);
			conn.commit();
			return result;
		}
		catch(InvocationTargetException e)
		{
			conn.rollback();
			throw e.getCause();
		}
		finally
		{
			TransactionSynchronizationManager.unbindConnection();
		}
	}
}
