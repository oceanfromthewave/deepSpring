package com.deepspring.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderEntity
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "item_name")
	private String itemName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private MemberEntity member;

	protected OrderEntity()
	{
	}

	public OrderEntity(String itemName, MemberEntity member)
	{
		this.itemName = itemName;
		this.member = member;
	}

	public Long getId()
	{
		return id;
	}

	public String getItemName()
	{
		return itemName;
	}

	public MemberEntity getMember()
	{
		return member;
	}
}
