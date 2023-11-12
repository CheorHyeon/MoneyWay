package com.wanted.moneyway.boundedContext.category.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ApiV1CategoryControllerTest {

	@Autowired
	private MockMvc mvc;

	@Test
	@DisplayName("GET /api/v1/categories 는 카테고리 목록 조회로 토큰이 필요하다")
	void t1() throws Exception {
		// When
		mvc.perform(get("/api/v1/member/categories"))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.resultCode").value("F-AuthenticationException"))
			.andExpect(jsonPath("$.msg").value("AT와 RT 둘 중 하나는 헤더에 포함시켜주세요"));
	}

	@Test
	@WithUserDetails("user1")
	@DisplayName("GET /api/v1/categories 사용자 로그인 시 카테고리 목록 조회 성공")
	void t2() throws Exception {
		// When
		mvc.perform(get("/api/v1/member/categories"))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.resultCode").value("F-AuthenticationException"))
			.andExpect(jsonPath("$.msg").value("AT와 RT 둘 중 하나는 헤더에 포함시켜주세요"));
	}
}
