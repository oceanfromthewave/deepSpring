package com.deepspring.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class SimpleJwt
{
	private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final String secret;
	private final long expirationMillis;

	public SimpleJwt(JwtProperties properties)
	{
		if(properties.getSecret() == null || properties.getSecret().isBlank())
		{
			throw new IllegalStateException("jwt.secret 설정이 없습니다");
		}
		this.secret = properties.getSecret();
		this.expirationMillis = properties.getExpirationMillis();
	}

	public String issue(String username)
	{
		String header = encode(HEADER_JSON);
		String payloadJson = "{\"sub\":\"" + username + "\",\"exp\":" + (System.currentTimeMillis() + expirationMillis) + "}";
		String payload = encode(payloadJson);

		return header + "." + payload + "." + sign(header + "." + payload);
	}

	public boolean verify(String token)
	{
		String[] parts = token.split("\\.");
		if(parts.length != 3)
		{
			return false;
		}
		return sign(parts[0] + "." + parts[1]).equals(parts[2]);
	}

	public String parseUsername(String token)
	{
		if(!verify(token))
		{
			throw new IllegalArgumentException("서명 검증 실패");
		}

		Map<String, Object> claims = payload(token);

		long exp = ((Number) claims.get("exp")).longValue();
		if(System.currentTimeMillis() > exp)
		{
			throw new IllegalStateException("토큰 만료");
		}

		return (String) claims.get("sub");
	}

	private String encode(String json)
	{
		return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
	}

	private String sign(String data)
	{
		try
		{
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> payload(String token)
	{
		try
		{
			byte[] decoded = Base64.getUrlDecoder().decode(token.split("\\.")[1]);
			return MAPPER.readValue(decoded, Map.class);
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("payload 파싱 실패", e);
		}
	}
}
