package com.wanted.moneyway.boundedContext.expenditure;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
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
public class ExpenditureControllerTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private MockMvc mvc;

	String token;
	@BeforeEach
	void setToken() {
		// "user1"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("user1");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
	}

	@Test
	@DisplayName("POST /api/v1/expenditure 은 지출 내역을 추가한다.")
	void t1() throws Exception {
		// When
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/expenditure")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
					.content("""
						{
							  "categoryId": "1",
							  "spendingPrice": "10000",
							  "memo": "식당 내기 짐",
							  "spendDate": "2023-11-14",
							  "isTotal": "false"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("지출 정보 저장 성공"))
			.andExpect(jsonPath("$.data.categoryId").value(1))
			.andExpect(jsonPath("$.data.spendingPrice").value(10_000))
			.andExpect(jsonPath("$.data.memo").value("식당 내기 짐"));
	}

	@Test
	@DisplayName("DELETE /api/v1/expenditure 은 지출 내역을 삭제한다.")
	void t2() throws Exception {
		// When
		ResultActions resultActions = mvc
			.perform(
				delete("/api/v1/expenditure")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
					.content("""
						{
							  "expenditureId": "1"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("삭제 성공"));
	}

	@Test
	@DisplayName("DELETE /api/v1/expenditure 은 작성자만 지출 내역을 삭제가 가능하다")
	void t3() throws Exception {
		// "user3"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("user3");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);

		// When
		ResultActions resultActions = mvc
			.perform(
				delete("/api/v1/expenditure")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
					.content("""
						{
							  "expenditureId": "1"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("F-1"))
			.andExpect(jsonPath("$.msg").value("내역 작성한 사용자만 삭제 가능합니다."));
	}
}
