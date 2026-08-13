package com.deepspring.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleJwtTest
{
	private SimpleJwt jwtWith(long expirationMillis)
	{
		JwtProperties properties = new JwtProperties();
		properties.setSecret("test-secret-key");
		properties.setExpirationMillis(expirationMillis);
		return new SimpleJwt(properties);
	}

	@Test
	@DisplayName("발급한 토큰에서 username을 꺼낼 수 있다")
	void issueAndParse()
	{
		SimpleJwt jwt = jwtWith(60_000);

		assertThat(jwt.parseUsername(jwt.issue("kim"))).isEqualTo("kim");
	}

	@Test
	@DisplayName("만료된 토큰은 파싱할 수 없다")
	void expiredToken()
	{
		SimpleJwt jwt = jwtWith(-1);
		String token = jwt.issue("kim");

		assertThatThrownBy(() -> jwt.parseUsername(token)).isInstanceOf(IllegalStateException.class).hasMessage("토큰 만료");
	}

	@Test
	@DisplayName("서명이 변조된 토큰은 파싱할 수 없다")
	void tamperedSignature()
	{
		SimpleJwt jwt = jwtWith(60_000);
		String token = jwt.issue("kim");
		String tampered = token.substring(0, token.length() - 5) + "AAAAA";

		assertThatThrownBy(() -> jwt.parseUsername(tampered)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("secret 설정이 없으면 Bean 생성 시점에 실패한다")
	void missingSecret()
	{
		JwtProperties properties = new JwtProperties();

		assertThatThrownBy(() -> new SimpleJwt(properties)).isInstanceOf(IllegalStateException.class);
	}
}
