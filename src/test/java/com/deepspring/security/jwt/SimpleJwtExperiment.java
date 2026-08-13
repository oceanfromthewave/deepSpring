package com.deepspring.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SimpleJwtExperiment
{
	public static void main(String[] args)
	{
		JwtProperties properties = new JwtProperties();
		properties.setSecret("experiment-secret");
		properties.setExpirationMillis(60_000);
		SimpleJwt jwt = new SimpleJwt(properties);

		String token = jwt.issue("kim");
		System.out.println("token = " + token);
		System.out.println("verify(원본) = " + jwt.verify(token));

		String tamperedSignature = token.substring(0, token.length() - 5) + "AAAAA";
		System.out.println("verify(서명 변조) = " + jwt.verify(tamperedSignature));

		String[] parts = token.split("\\.");
		System.out.println("decoded payload = " + new String(Base64.getUrlDecoder().decode(parts[1])));

		System.out.println();
		System.out.println("=== payload 조작: kim -> admin (권한 상승 시도) ===");
		String originalPayload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		String hackedPayload = originalPayload.replace("\"sub\":\"kim\"", "\"sub\":\"admin\"");
		String hackedToken = parts[0] + "."
				+ Base64.getUrlEncoder().withoutPadding().encodeToString(hackedPayload.getBytes(StandardCharsets.UTF_8))
				+ "." + parts[2];

		System.out.println("verify(조작 토큰) = " + jwt.verify(hackedToken));
		try
		{
			System.out.println("parseUsername = " + jwt.parseUsername(hackedToken));
		}
		catch (Exception e)
		{
			System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
}
