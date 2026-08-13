package com.deepspring.security.jwt;

import com.deepspring.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@ContextConfiguration(classes = {LoginController.class, SecurityConfig.class, TestJwtConfig.class})
class LoginControllerTest
{
	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	AuthenticationManager authenticationManager;

	@Test
	@DisplayName("로그인 성공하면 JWT를 반환한다")
	void loginSuccess() throws Exception
	{
		System.out.println(">>> LoginControllerTest, MockMvc = " + System.identityHashCode(mockMvc));
		given(authenticationManager.authenticate(any())).willReturn(new UsernamePasswordAuthenticationToken("kim", null, List.of()));

		mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"kim\",\"password\":\"1234\"}"))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("비밀번호가 틀리면 401을 반환한다")
	void loginFail() throws Exception
	{
		given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("자격 증명에 실패하였습니다."));

		mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"kim\",\"password\":\"wrong\"}"))
				.andExpect(status().isUnauthorized());
	}
}
