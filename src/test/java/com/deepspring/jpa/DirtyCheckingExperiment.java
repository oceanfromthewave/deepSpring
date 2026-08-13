package com.deepspring.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class DirtyCheckingExperiment
{
	public static void main(String[] args)
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("deepspring");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		System.out.println("=== 준비: id=2 저장 ===");
		tx.begin();
		em.persist(new MemberEntity(2L, "lee", "BASIC"));
		tx.commit();

		em.clear();

		System.out.println();
		System.out.println("=== Dirty Checking 실험: find 후 setter만 호출, save() 호출 안 함 ===");
		tx.begin();
		MemberEntity found = em.find(MemberEntity.class, 2L);
		System.out.println("변경 전 name = " + found.getName());

		found.setName("lee-updated");
		System.out.println("setName() 호출함. save()/persist()/update() 아무것도 호출 안 함.");

		System.out.println("--- commit 호출 (여기서 flush 발생) ---");
		tx.commit();

		em.close();
		emf.close();
	}
}
