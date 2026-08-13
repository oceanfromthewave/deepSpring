package com.deepspring.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class LazyLoadingExperiment
{
	public static void main(String[] args)
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("deepspring");

		System.out.println("=== 0. 준비: member + order 저장 ===");
		EntityManager prep = emf.createEntityManager();
		prep.getTransaction().begin();
		MemberEntity member = new MemberEntity(3L, "park", "VIP");
		prep.persist(member);
		prep.persist(new OrderEntity("keyboard", member));
		prep.getTransaction().commit();
		prep.close();

		System.out.println();
		System.out.println("=== 1. order 조회 -> member 는 Proxy 인지 확인 ===");
		EntityManager em1 = emf.createEntityManager();
		OrderEntity order = em1.find(OrderEntity.class, 1L);
		System.out.println("order 조회 SQL 만 나갔어야 함 (member SELECT 아직 없음)");

		MemberEntity lazyMember = order.getMember();
		System.out.println("getMember() 반환 클래스 = " + lazyMember.getClass());
		System.out.println("실제 MemberEntity 랑 같은 클래스인가? " + (lazyMember.getClass() == MemberEntity.class));

		System.out.println("--- 이제 getName() 호출 -> 이 시점에 SELECT 발생(초기화) ---");
		System.out.println("member.getName() = " + lazyMember.getName());
		em1.close();

		System.out.println();
		System.out.println("=== 2. em.close() 이후 Lazy 필드 접근 -> LazyInitializationException ===");
		EntityManager em2 = emf.createEntityManager();
		OrderEntity order2 = em2.find(OrderEntity.class, 1L);
		em2.close();
		try
		{
			order2.getMember().getName();
			System.out.println("예외 안 터짐 (예상과 다름)");
		}
		catch (Exception e)
		{
			System.out.println("예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
		}

		emf.close();
	}
}
