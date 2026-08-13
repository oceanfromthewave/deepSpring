package com.deepspring.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TxAsyncService
{
	@Async
	public void asyncCheck(String label)
	{
		System.out.println("[" + Thread.currentThread()
				.getName() + "] " + label + " | transaction active = " + TransactionSynchronizationManager.isActualTransactionActive() + " | transaction name = " + TransactionSynchronizationManager.getCurrentTransactionName());
	}
}
