package com.deepspring.async;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TxCallerService
{
	private final TxAsyncService txAsyncService;
	private final ApplicationEventPublisher eventPublisher;

	public TxCallerService(TxAsyncService txAsyncService, ApplicationEventPublisher eventPublisher)
	{
		this.txAsyncService = txAsyncService;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public void doWorkInTransaction()
	{
		System.out.println("[" + Thread.currentThread()
				.getName() + "] 호출자(@Transactional)" + " | transaction active = " + TransactionSynchronizationManager.isActualTransactionActive() + " | transaction name = " + TransactionSynchronizationManager.getCurrentTransactionName());

		txAsyncService.asyncCheck("비동기 작업");

		System.out.println("[" + Thread.currentThread().getName() + "] 호출자 종료 (아직 커밋 전)");
	}

	@Transactional
	public void registerSuccess(String username)
	{
		System.out.println("[" + Thread.currentThread().getName() + "] registerSuccess: 이벤트 발행 직전");
		eventPublisher.publishEvent(new UserRegisteredEvent(username));
		System.out.println("[" + Thread.currentThread().getName() + "] registerSuccess: 이벤트 발행 직후 (아직 커밋 전)");
	}

	@Transactional
	public void registerFail(String username)
	{
		System.out.println("[" + Thread.currentThread().getName() + "] registerFail: 이벤트 발행");
		eventPublisher.publishEvent(new UserRegisteredEvent(username));
		throw new RuntimeException("가입 처리 중 실패 - 롤백됨");
	}
}
