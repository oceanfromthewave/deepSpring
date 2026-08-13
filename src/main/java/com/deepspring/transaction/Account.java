package com.deepspring.transaction;

public class Account
{
	private final String name;
	private int balance;

	public Account(String name, int balance)
	{
		this.name = name;
		this.balance = balance;
	}

	public void withdraw(int amount)
	{
		if(balance < amount)
		{
			throw new IllegalStateException(name + "잔액 부족");
		}
		balance -= amount;
	}

	public void deposit(int amount)
	{
		balance += amount;
	}

	public String getName()
	{
		return name;
	}

	public int getBalance()
	{
		return balance;
	}
}
