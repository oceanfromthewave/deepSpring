package com.deepspring.validation;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserValidationController
{

	@PostMapping("/users")
	public String create(@Valid @RequestBody UserCreateRequest request)
	{
		if ("dup@test.com".equals(request.getEmail()))
		{
			throw new DuplicateEmailException(request.getEmail());
		}
		return "created: name=" + request.getName() + ", email=" + request.getEmail() + ", age=" + request.getAge();
	}
}
