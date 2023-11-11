package com.wanted.moneyway.boundedContext.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ApiV1MemberControllerTest {
	@Autowired
	private MockMvc mvc;

	@Test
	@DisplayName("POST /api/v1/member/signup은 회원가입 URL 이다.")
	void t1() throws Exception {
		// When
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/member/signup")
					.content("""
						{
						    "username": "puar12",
						    "password": "cjfgus0513"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("회원가입 완료 로그인 후 이용해주세요!"));
	}

	@Test
	@DisplayName("회원가입 시 비밀번호 제약조건을 지키지 않으면 가입이 되지 않는다.")
	void t2() throws Exception {
		// When
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/member/signup")
					.content("""
						{
						    "username": "puar12",
						    "password": "1234"
						}
						""".stripIndent())
					// JSON 형태로 보내겠다
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("F-1"))
			.andExpect(jsonPath("$.msg").value("비밀번호는 10자 이상 입력해야 하며, 숫자로만 이루어 질 수 없으며 3회 이상 연속되는 문자를 사용할 수 없습니다."));
	}

	@Test
	@DisplayName("POST /api/v1/member/login을 통해 로그인 시 JWT 발행")
	void t3() throws Exception {
		// When
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/member/login")
					.content("""
						{
						    "username": "user1",
						    "password": "1234"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("토큰 발급 성공"))
			.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
	}
}
