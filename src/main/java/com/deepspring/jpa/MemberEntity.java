package com.deepspring.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
public class MemberEntity {

	@Id
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "grade")
	private String grade;

	protected MemberEntity() {
	}

	public MemberEntity(Long id, String name, String grade) {
		this.id = id;
		this.name = name;
		this.grade = grade;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGrade() {
		return grade;
	}
}
