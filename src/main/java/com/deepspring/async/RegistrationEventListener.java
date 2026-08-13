package com.deepspring.async;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class RegistrationEventListener
{
	@EventListener
	public void onPlainEvent(UserRegisteredEvent event)
	{
		print("@EventListener", event);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onAfterCommit(UserRegisteredEvent event)
	{
		print("@TransactionalEventListener(AFTER_COMMIT)", event);
	}

	private void print(String label, UserRegisteredEvent event)
	{
		System.out.println("[" + Thread.currentThread()
				.getName() + "] " + label + " | user = " + event.username() + " | transaction active = " + TransactionSynchronizationManager.isActualTransactionActive());
	}
}
