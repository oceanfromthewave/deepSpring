package com.deepspring.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class DetachedStateExperiment
{
	public static void main(String[] args)
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("deepspring");

		System.out.println("=== 0. 준비: id=2 저장 ===");
		EntityManager prep = emf.createEntityManager();
		prep.getTransaction().begin();
		prep.persist(new MemberEntity(2L, "lee", "BASIC"));
		prep.getTransaction().commit();
		prep.close();

		System.out.println("=== 1. find 후 em.close() -> Detached 상태서 필드 변경 ===");
		EntityManager em1 = emf.createEntityManager();
		em1.getTransaction().begin();
		MemberEntity entity = em1.find(MemberEntity.class, 2L);
		em1.getTransaction().commit();
		em1.close();

		System.out.println("em1 close 됨. entity 는 이제 Detached.");
		entity.setName("changed-but-detached");
		System.out.println("Detached 상태서 setName 호출함. flush 대상 아님.");

		System.out.println();
		System.out.println("=== 새 EntityManager 로 다시 조회 -> DB 값 그대로인지 확인 ===");
		EntityManager em2 = emf.createEntityManager();
		MemberEntity reloaded = em2.find(MemberEntity.class, 2L);
		System.out.println("재조회 name = " + reloaded.getName() + "  (Detached 변경 반영 안 됐으면 이전 값)");
		em2.close();

		System.out.println();
		System.out.println("=== merge() 사용 -> Detached 엔티티를 다시 Managed 로 합치기 ===");
		EntityManager em3 = emf.createEntityManager();
		em3.getTransaction().begin();
		MemberEntity managedCopy = em3.merge(entity);
		System.out.println("merge() 반환값과 원본 entity 같은 객체인가? " + (managedCopy == entity));
		em3.getTransaction().commit();
		em3.close();

		System.out.println();
		System.out.println("=== 최종 확인 ===");
		EntityManager em4 = emf.createEntityManager();
		MemberEntity finalCheck = em4.find(MemberEntity.class, 2L);
		System.out.println("최종 name = " + finalCheck.getName());
		em4.close();

		emf.close();
	}
}
