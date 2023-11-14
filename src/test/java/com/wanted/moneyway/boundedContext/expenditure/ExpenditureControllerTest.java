package com.wanted.moneyway.boundedContext.expenditure;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

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

	@Test
	@DisplayName("GET /api/v1/expenditure/{id} 는 사용자가 작성한 지출 내역의 상세 내용을 반환한다.")
	void t4() throws Exception {
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/1")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("내역 조회 성공"))
			.andExpect(jsonPath("$.data.expenditureId").value(1))
			.andExpect(jsonPath("$.data.categoryId").value(1))
			.andExpect(jsonPath("$.data.spendingPrice").value(10000))
			.andExpect(jsonPath("$.data.memo").value("식당 내기 짐"))
			.andExpect(jsonPath("$.data.spendDate").value("2023-11-01"))
			.andExpect(jsonPath("$.data.isTotal").value(true));
	}

	@Test
	@DisplayName("GET /api/v1/expenditure/{id} 는 다른 사용자가 작성한 내역을 검사할 수 없다.")
	void t5() throws Exception {
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/4")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("F-1"))
			.andExpect(jsonPath("$.msg").value("지출 내역 작성자가 아닙니다."));
	}

	@Test
	@DisplayName("GET /api/v1/expenditure/{id} 는 삭제되었거나 존재하지 않는 지출내역의 정보를 조회할 수 없다.")
	void t6() throws Exception {
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/1052")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("F-1"))
			.andExpect(jsonPath("$.msg").value("이미 삭제되었거나 존재하지 않는 내역입니다."));
	}

	@Test
	@DisplayName("GET /api/v1/expenditure 는 전체 지출 총액, 카테고리별 지출 총액 조회가 가능하다")
	void t7_1() throws Exception {
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("조회 성공"))
			.andExpect(jsonPath("$.data.totalSpending").value("200000"))
			.andExpect(jsonPath("$.data.spendingByCategory[0].categoryName").value("식비"))
			.andExpect(jsonPath("$.data.spendingByCategory[0].spending").value(100000))
			.andExpect(jsonPath("$.data.spendingByCategory[1].categoryName").value("카페/간식"))
			.andExpect(jsonPath("$.data.spendingByCategory[1].spending").value(100000));
	}

	@Test
	@DisplayName("GET /api/v1/expenditure 는 위 테스트에서 금액 뿐 아니라 카테고리별 지출 내역 역시 조회가 가능하다")
	void t7_2() throws Exception {
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("조회 성공"))
			.andExpect(jsonPath("$.data.expenditures.content[0].memo").value("테스트 식비1"))
			.andExpect(jsonPath("$.data.expenditures.content[0].spendingPrice").value(10_000))
			.andExpect(jsonPath("$.data.expenditures.content[1].memo").value("테스트 카페/간식1"))
			.andExpect(jsonPath("$.data.expenditures.content[1].spendingPrice").value(10_000));
	}
}
