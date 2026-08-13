package com.deepspring.servlet;

public class HelloController
{
	@GetMapping("/hello")
	public String Hello()
	{
		return "hello";
	}

	@GetMapping("/bye")
	public String bye()
	{
		return "bye";
	}

	@GetMapping("/greet")
	public String greet(@RequestParam("name") String name)
	{
		return "hi" + name;
	}

	@GetMapping("/greet-json")
	public GreetResponse greetJson(@RequestParam("name") String name)
	{
		return new GreetResponse("hi" + name);
	}
}
