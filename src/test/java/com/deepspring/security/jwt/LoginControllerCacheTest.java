package com.deepspring.security.jwt;

import com.deepspring.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * LoginControllerTest와 애노테이션 구성이 100% 동일함.
 * TestContext 캐시가 동작하면 Context를 새로 만들지 않고 재사용해야 함.
 */
@WebMvcTest(LoginController.class)
@ContextConfiguration(classes = {LoginController.class, SecurityConfig.class, TestJwtConfig.class})
class LoginControllerCacheTest
{
	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	AuthenticationManager authenticationManager;

	// 캐시 miss 유발: 이 mock 하나가 캐시 키를 바꿈
	@MockitoBean
	org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

	@Test
	@DisplayName("Context가 재사용되는지 확인용 - MockMvc 주입만 검증")
	void contextReuse()
	{
		System.out.println(">>> LoginControllerCacheTest, MockMvc = " + System.identityHashCode(mockMvc));
	}
}
