package com.deepspring.validation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class UserCreateRequest
{
	private @NotBlank String name;
	private @Email String email;
	private @Min(0) Integer age;

	public UserCreateRequest()
	{

	}

	public UserCreateRequest(String name, String email, Integer age)
	{
		this.name = name;
		this.email = email;
		this.age = age;
	}

	public String getName()
	{
		return name;
	}

	public String getEmail()
	{
		return email;
	}

	public Integer getAge()
	{
		return age;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public void setAge(Integer age)
	{
		this.age = age;
	}
}
