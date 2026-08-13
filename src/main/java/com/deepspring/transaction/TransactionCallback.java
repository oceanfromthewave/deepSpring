package com.deepspring.transaction;

public interface TransactionCallback
{
	void doInTransaction(FakeConnection conn);
}
