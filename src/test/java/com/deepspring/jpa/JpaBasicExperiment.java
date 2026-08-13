package com.deepspring.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class JpaBasicExperiment
{
	public static void main(String[] args)
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("deepspring");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		System.out.println("--- persist 호출 ---");
		em.persist(new MemberEntity(1L, "kim", "VIP"));
		System.out.println("--- persist 직후, 아직 commit 전 ---");

		System.out.println("--- find 호출 (Persistence Context 안에서) ---");
		MemberEntity found = em.find(MemberEntity.class, 1L);
		System.out.println("find 결과 = id=" + found.getId() + ", name=" + found.getName() + ", grade=" + found.getGrade());

		System.out.println("--- commit 호출 ---");
		tx.commit();

		em.close();
		emf.close();
	}
}
