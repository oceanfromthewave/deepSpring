package com.deepspring.transaction;

public class TransactionSynchronizationManager
{

	private static final ThreadLocal<FakeConnection> CONNECTION_HOLDER = new ThreadLocal<>();

	public static void bindConnection(FakeConnection conn)
	{
		CONNECTION_HOLDER.set(conn);
	}

	public static FakeConnection getConnection()
	{
		FakeConnection conn = CONNECTION_HOLDER.get();
		if(conn == null)
		{
			throw new IllegalStateException("현재 스레드에 바인딩된 Transaction Connection 없음");
		}
		return conn;
	}

	public static void unbindConnection()
	{
		CONNECTION_HOLDER.remove();
	}
}

