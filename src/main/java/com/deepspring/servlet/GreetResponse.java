package com.deepspring.servlet;

public class GreetResponse
{
	private final String message;

	public GreetResponse(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}
}
