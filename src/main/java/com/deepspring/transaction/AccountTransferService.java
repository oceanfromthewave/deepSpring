package com.deepspring.transaction;

public class AccountTransferService implements AccountTransferServiceInterface
{

	@Override
	public void transfer(Account from, Account to, int amount)
	{
		FakeConnection conn = TransactionSynchronizationManager.getConnection();

		conn.execute(() -> from.withdraw(amount), () -> from.deposit(amount));
		validate(to);
		conn.execute(() -> to.deposit(amount), () -> to.withdraw(amount));
	}

	private void validate(Account to)
	{
		if(to.getName().equals("broken"))
		{
			throw new IllegalStateException("이체 중간 장애 발생");
		}
	}
}

