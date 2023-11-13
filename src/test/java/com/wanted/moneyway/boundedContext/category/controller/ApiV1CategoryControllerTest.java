package com.wanted.moneyway.boundedContext.category.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.wanted.moneyway.base.jwt.JwtProvider;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.service.MemberService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ApiV1CategoryControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private JwtProvider jwtProvider;

	@Test
	@DisplayName("GET /api/v1/categories 는 카테고리 목록 조회로 토큰이 필요하다")
	void t1() throws Exception {
		// When
		mvc.perform(get("/api/v1/categories"))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.resultCode").value("F-AuthenticationException"))
			.andExpect(jsonPath("$.msg").value("AT와 RT 둘 중 하나는 헤더에 포함시켜주세요"));
	}

	@Test
	@DisplayName("GET /api/v1/categories 사용자 로그인 시 카테고리 목록 조회 성공")
	void t2() throws Exception {
		// "user1"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("user1");
		String token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);

		// When
		ResultActions resultActions = mvc.perform(
			get("/api/v1/categories")
				.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
		).andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("카테고리 목록 조회 성공"))
			.andExpect(jsonPath("$.data[0].nameH").value("식비"));
	}
}
