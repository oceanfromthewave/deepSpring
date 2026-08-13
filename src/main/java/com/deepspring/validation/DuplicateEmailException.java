package com.deepspring.validation;

public class DuplicateEmailException extends RuntimeException
{
	public DuplicateEmailException(String email)
	{
		super("이미 사용중인 이메일: " + email);
	}
}
