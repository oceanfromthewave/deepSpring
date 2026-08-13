package com.deepspring.jpa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

public class SpringDataJpaExperiment
{
	@SpringBootApplication
	@EntityScan(basePackages = "com.deepspring.jpa")
	@EnableJpaRepositories(basePackages = "com.deepspring.jpa")
	static class TestApp
	{
	}

	public static void main(String[] args)
	{
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestApp.class)
				.properties(
						"spring.datasource.url=jdbc:h2:mem:experiment;DB_CLOSE_DELAY=-1",
						"spring.datasource.driver-class-name=org.h2.Driver",
						"spring.jpa.hibernate.ddl-auto=update",
						"spring.jpa.show-sql=true",
						"spring.main.web-application-type=none")
				.run(args);

		MemberJpaRepository repo = ctx.getBean(MemberJpaRepository.class);

		System.out.println("repo 실제 클래스 = " + repo.getClass());
		System.out.println("JDK Dynamic Proxy 인가? " + java.lang.reflect.Proxy.isProxyClass(repo.getClass()));

		System.out.println();
		System.out.println("=== save() ===");
		repo.save(new MemberEntity(1L, "kim", "VIP"));
		repo.save(new MemberEntity(2L, "lee", "VIP"));
		repo.save(new MemberEntity(3L, "park", "NORMAL"));

		System.out.println();
		System.out.println("=== findById() ===");
		System.out.println(repo.findById(1L).map(MemberEntity::getName).orElse("없음"));

		System.out.println();
		System.out.println("=== findAll() ===");
		System.out.println("개수 = " + repo.findAll().size());

		System.out.println();
		System.out.println("=== findByGrade(\"VIP\") ===");
		repo.findByGrade("VIP").forEach(m -> System.out.println(m.getName()));

		System.out.println();
		System.out.println("=== findByNameContaining(\"a\") ===");
		repo.findByNameContaining("a").forEach(m -> System.out.println(m.getName()));

		System.out.println();
		System.out.println("=== findByGradeOrderByNameAsc(\"VIP\") ===");
		repo.findByGradeOrderByNameAsc("VIP").forEach(m -> System.out.println(m.getName()));

		System.out.println();
		System.out.println("=== findByGradeAndNameKeyword(\"VIP\", \"i\") ===");
		repo.findByGradeAndNameKeyword("VIP", "i").forEach(m -> System.out.println(m.getName()));

		System.out.println();
		System.out.println("=== findByGrade(\"VIP\", Pageable) - page 0, size 1 ===");
		Page<MemberEntity> page0 = repo.findByGrade("VIP", PageRequest.of(0, 1, Sort.by("name")));
		System.out.println("content = " + page0.getContent().stream().map(MemberEntity::getName).toList());
		System.out.println("totalElements = " + page0.getTotalElements());
		System.out.println("totalPages = " + page0.getTotalPages());

		System.out.println();
		System.out.println("=== findByGrade(\"VIP\", Pageable) - page 1, size 1 ===");
		Page<MemberEntity> page1 = repo.findByGrade("VIP", PageRequest.of(1, 1, Sort.by("name")));
		System.out.println("content = " + page1.getContent().stream().map(MemberEntity::getName).toList());

		ctx.close();
	}
}
