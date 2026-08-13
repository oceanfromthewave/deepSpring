package com.deepspring.transaction;

public interface AccountTransferServiceInterface
{
	void transfer(Account from, Account to, int amount);
}
