package com.deepspring.transaction;

public class TransactionTemplate
{
	public void execute(TransactionCallback callback)
	{
		FakeConnection conn = new FakeConnection();

		try
		{
			callback.doInTransaction(conn);
			conn.commit();
		}
		catch(RuntimeException e)
		{
			conn.rollback();
			throw e;
		}
	}
}
