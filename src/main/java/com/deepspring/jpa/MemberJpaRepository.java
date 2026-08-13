package com.deepspring.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long>
{
	List<MemberEntity> findByGrade(String grade);

	List<MemberEntity> findByNameContaining(String keyword);

	List<MemberEntity> findByGradeOrderByNameAsc(String grade);

	@Query("select m from MemberEntity m where m.grade = :grade and m.name like %:keyword%")
	List<MemberEntity> findByGradeAndNameKeyword(@Param("grade") String grade, @Param("keyword") String keyword);

	Page<MemberEntity> findByGrade(String grade, Pageable pageable);
}
