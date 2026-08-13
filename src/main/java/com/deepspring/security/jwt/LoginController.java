package com.deepspring.security.jwt;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController
{
	private final AuthenticationManager authenticationManager;
	private final SimpleJwt simpleJwt;

	public LoginController(AuthenticationManager authenticationManager, SimpleJwt simpleJwt)
	{
		this.authenticationManager = authenticationManager;
		this.simpleJwt = simpleJwt;
	}

	@PostMapping("/login")
	public String login(@RequestBody LoginRequest request)
	{
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

		return simpleJwt.issue(authentication.getName());
	}

	public record LoginRequest(String username, String password)
	{
	}
}
