package com.wanted.moneyway.boundedContext.plan.controller;

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

import com.wanted.moneyway.base.jwt.JwtProvider;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.service.MemberService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ApiV1PlanControllertest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private JwtProvider jwtProvider;

	@Test
	@DisplayName("POST /api/v1/plan 은 예산 등록 URL 이다.")
	void t1() throws Exception {
		// "user1"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("user4");
		String token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/plan")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
					.content("""
						{
						    "food": "100000",
						    "cafe": "50000",
						    "education" : "100000",
						    "dwelling" : "300000",
						    "communication" : "100000",
						    "shopping" : "30000",
						    "transfer" : "100000",
						    "others" : "300000"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("지출 계획 생성 완료"))
			.andExpect(jsonPath("$.data[0].budget").value(100_000));
	}

	@Test
	@DisplayName("POST /api/v1/plan 은 예산 등록 URL로 일부 속성만 입력해도 나머지는 0원으로 자동 입력된다.")
	void t2() throws Exception {
		// "user1"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("user4");
		String token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/plan")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
					.content("""
						{
						    "food": "100000",
						    "cafe": "50000",
						    "dwelling" : "300000",
						    "communication" : "100000",
						    "shopping" : "30000",
						    "transfer" : "100000",
						    "others" : "300000"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("지출 계획 생성 완료"))
			.andExpect(jsonPath("$.data[2].category.nameE").value("education"))
			.andExpect(jsonPath("$.data[2].budget").value(0));
	}

	/*
		기존 user1의 식비 예산 10만원 -> 20만원 변경 테스트
		Web View에서 예산 수정 메뉴 -> 전체 설정 예산 편집 칸 -> 수정한것 그대로 반영을 생각하고 설계해서 모든 카테고리 포함
	 */
	@Test
	@DisplayName("POST /api/v1/plan 은 기존에 등록된 사용자의 예산을 변경할 수도 있다.")
	void t3() throws Exception {
		// "user1"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("user1");
		String token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/plan")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
					.content("""
						{
						    "food": "200000",
						    "cafe": "100000",
						    "education" : "100000",
						    "dwelling" : "100000",
						    "communication" : "100000",
						    "shopping" : "100000",
						    "transfer" : "100000",
						    "others" : "100000"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("지출 계획 수정 완료"))
			.andExpect(jsonPath("$.data[0].category.nameE").value("food"))
			.andExpect(jsonPath("$.data[0].budget").value(200_000));
	}

	@Test
	@DisplayName("POST /api/v1/plan/recommend 은 예산 추천 URL로 총액을 입력하면 예산별 금액을 추천해준다.")
	void t4() throws Exception {
		// "user1"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("user1");
		String token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/plan/recommend")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
					.content("""
						{
						    "totalPrice": "1000000"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("항목별 추천 금액"))
			.andExpect(jsonPath("$.data.food").value("189904"))
			.andExpect(jsonPath("$.data.cafe").value("112981"));
	}

}
