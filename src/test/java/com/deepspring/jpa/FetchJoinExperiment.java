package com.deepspring.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public class FetchJoinExperiment
{
	public static void main(String[] args)
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("deepspring");

		System.out.println("=== 0. 준비: member 3명 + order 3건 ===");
		EntityManager prep = emf.createEntityManager();
		prep.getTransaction().begin();
		MemberEntity m1 = new MemberEntity(20L, "kim", "VIP");
		MemberEntity m2 = new MemberEntity(21L, "lee", "BASIC");
		MemberEntity m3 = new MemberEntity(22L, "park", "VIP");
		prep.persist(m1);
		prep.persist(m2);
		prep.persist(m3);
		prep.persist(new OrderEntity("mouse", m1));
		prep.persist(new OrderEntity("monitor", m2));
		prep.persist(new OrderEntity("desk", m3));
		prep.getTransaction().commit();
		prep.close();

		System.out.println();
		System.out.println("=== 1. fetch join 으로 order + member 한번에 조회 ===");
		EntityManager em = emf.createEntityManager();
		List<OrderEntity> orders = em.createQuery(
				"select o from OrderEntity o join fetch o.member", OrderEntity.class)
				.getResultList();
		System.out.println("order 개수 = " + orders.size() + " (여기서 SELECT 1번으로 member 까지 다 채워졌어야 함)");

		System.out.println();
		System.out.println("=== 2. 루프 돌면서 getMember().getName() -> 추가 SELECT 없어야 함 ===");
		for (OrderEntity order : orders)
		{
			System.out.println(order.getItemName() + " -> " + order.getMember().getName());
		}
		System.out.println("추가 SELECT 없이 끝났으면 fetch join 성공");

		em.close();
		emf.close();
	}
}
