package com.deepspring.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public class NPlusOneExperiment
{
	public static void main(String[] args)
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("deepspring");

		System.out.println("=== 0. 준비: member 3명 + order 3건(각각 다른 member) ===");
		EntityManager prep = emf.createEntityManager();
		prep.getTransaction().begin();
		MemberEntity m1 = new MemberEntity(10L, "kim", "VIP");
		MemberEntity m2 = new MemberEntity(11L, "lee", "BASIC");
		MemberEntity m3 = new MemberEntity(12L, "park", "VIP");
		prep.persist(m1);
		prep.persist(m2);
		prep.persist(m3);
		prep.persist(new OrderEntity("mouse", m1));
		prep.persist(new OrderEntity("monitor", m2));
		prep.persist(new OrderEntity("desk", m3));
		prep.getTransaction().commit();
		prep.close();

		System.out.println();
		System.out.println("=== 1. order 목록 조회 (SELECT 1번) ===");
		EntityManager em = emf.createEntityManager();
		List<OrderEntity> orders = em.createQuery("select o from OrderEntity o", OrderEntity.class)
				.getResultList();
		System.out.println("order 개수 = " + orders.size() + " (여기까지 SELECT 1번만 나갔어야 함)");

		System.out.println();
		System.out.println("=== 2. 루프 돌면서 order.getMember().getName() 호출 -> N번 추가 SELECT ===");
		for (OrderEntity order : orders)
		{
			System.out.println(order.getItemName() + " -> " + order.getMember().getName());
		}
		System.out.println("총 SELECT = 1(order 목록) + " + orders.size() + "(member 각각) = N+1");

		em.close();
		emf.close();
	}
}
