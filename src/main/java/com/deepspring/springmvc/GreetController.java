package com.deepspring.springmvc;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetController
{
	@GetMapping("/greet")
	public String greet(@RequestParam("name") String name)
	{
		return "hi" + name;
	}
}
