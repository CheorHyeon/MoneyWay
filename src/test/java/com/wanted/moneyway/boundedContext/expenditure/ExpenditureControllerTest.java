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

	@Test
	@DisplayName("GET /api/v1/expenditure/remaining 는 이번달 남은 예산(총액, 카테고리별) 조회가 가능하다.")
	void t8() throws Exception {
		// "cheorhyeon"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("cheorhyeon");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/remaining")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("이번달 남은 지출액 조회 성공"))
			.andExpect(jsonPath("$.data.totalRemainingPrice").value(870000))
			.andExpect(jsonPath("$.data.remainingPriceByCategoy[0].categoryId").value(1))
			.andExpect(jsonPath("$.data.remainingPriceByCategoy[0].categoryName").value("식비"))
			.andExpect(jsonPath("$.data.remainingPriceByCategoy[0].spending").value(390000));
	}

	/*
		남은 금액 / 말일까지 남은 일수로 계산하여 결과를 잘 반환하는지 테스트합니다.
		실제로는 1일별 사용 추천액을 테스트에 넣어야 하지만, 테스트를 수행하는 시점에 따라서 추천 금액이 달라지기에
		총 금액과 메세지만 테스트합니다.
	 */
	@Test
	@DisplayName("GET /api/v1/expenditure/recommend 는 오늘 사용할 예산을 추천해준다.")
	void t9() throws Exception {
		// "cheorhyeon"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("cheorhyeon");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/recommend")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("금일 지출액 추천 성공"))
			.andExpect(jsonPath("$.data.totalRemainingPrice").value(870000));
	}

	@Test
	@DisplayName("PATCH /api/v1/expenditure/{id} 는 지출 내역을 수정하며, 일부 속성만 줘도 변경된다.")
	void t10() throws Exception {
		// "cheorhyeon"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("cheorhyeon");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		// 기존 10만원 -> 4만원 변경 시도
		ResultActions resultActions = mvc
			.perform(
				patch("/api/v1/expenditure/31")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
					.content("""
						{
							  "spendingPrice": "40000"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("지출 내역 변경 성공"))
			.andExpect(jsonPath("$.data.spendingPrice").value(40000));
	}

	/*
		오늘 지출한 총액, 카테고리별 금액을 오늘 추천해준 금액과 비교하는 결과를 반환합니다.
		실제로는 차이 금액과 위험도로 테스트를 해야하지만, 테스트를 수행하는 시점에 따라서 추천 금액과 위험도가 달라지기 때문에
		오늘 지출한 총 금액과 메세지만 테스트합니다.
 	*/
	@Test
	@DisplayName("GET /api/v1/expenditure/today 는 오늘 지출 내역과 위험도를 알려준다.")
	void t11() throws Exception {
		// "cheorhyeon"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("cheorhyeon");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/today")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("오늘 지출 내역과 위험도 반환 성공"))
			.andExpect(jsonPath("$.data.todayTotalPrice").value(430000));
	}

	/*
		지출 통계 첫번째 기능 : 지난달 대비 총액, 카테고리 별 소비율을 통계내는 기능을 구현합니다.
		오늘이 10일차라면, 지난달 10일차 까지의 데이터를 대상으로 비교헙니다.
		ex) 식비 지난달 대비 150%
		- case 1 : 총액 및 비율
		- case 2 : 카테고리별 비율
		- case 3 : 이번달 신규 카테고리의 경우 비율 없이 금액만 표시
 	*/
	@Test
	@DisplayName("GET /api/v1/expenditure/statistics/lastmonth 는 현재 일자의 지난달과 이번달 대비 지출금액과 비율을 반환합니다.")
	void t12_1() throws Exception {
		// "cheorhyeon"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("cheorhyeon");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/statistics/lastmonth")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("지난달 대비 오늘 일자 기준 사용량 통계 데이터 추출 성공"))
			.andExpect(jsonPath("$.data.currentTotal").value(430000))
			.andExpect(jsonPath("$.data.amonthAgoTotal").value(106000))
			.andExpect(jsonPath("$.data.totalRatio").value(405.7));
	}

	@Test
	@DisplayName("GET /api/v1/expenditure/statistics/lastmonth 는 현재 일자의 지난달과 이번달 대비 각 카테고리별 지출금액과 비율 또한 반환합니다.")
	void t12_2() throws Exception {
		// "cheorhyeon"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("cheorhyeon");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/statistics/lastmonth")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("지난달 대비 오늘 일자 기준 사용량 통계 데이터 추출 성공"))
			.andExpect(jsonPath("$.data.categoryRatioList[0].categoryName").value("식비"))
			.andExpect(jsonPath("$.data.categoryRatioList[0].todaySpending").value(110000))
			.andExpect(jsonPath("$.data.categoryRatioList[0].amonthAgoSpending").value(100000))
			.andExpect(jsonPath("$.data.categoryRatioList[0].compareRatio").value(110));
	}

	@Test
	@DisplayName("GET /api/v1/expenditure/statistics/lastmonth 는 지난달에 소비하지 않았지만 이번달에 소비한 카테고리 별 금액은 비율 없이 금액만 반환합니다.")
	void t12_3() throws Exception {
		// "cheorhyeon"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("cheorhyeon");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/statistics/lastmonth")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("지난달 대비 오늘 일자 기준 사용량 통계 데이터 추출 성공"))
			.andExpect(jsonPath("$.data.categoryRatioList[3].categoryName").value("통신"))
			.andExpect(jsonPath("$.data.categoryRatioList[3].todaySpending").value(20000))
			.andExpect(jsonPath("$.data.categoryRatioList[3].amonthAgoSpending").value(0))
			.andExpect(jsonPath("$.data.categoryRatioList[3].compareRatio").isEmpty());
	}

	/*
		7일전 지출과 오늘 지출 총액을 비교한 결과를 반환합니다.
	 */
	@Test
	@DisplayName("GET /api/v1/expenditure/statistics/lastweek 는 7일전 지출과 오늘 지출을 비교합니다.")
	void t13() throws Exception {
		// "user3"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("user3");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/statistics/lastweek")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("지난 요일과 총액 비교 성공"))
			.andExpect(jsonPath("$.data.todayTotal").value(200000))
			.andExpect(jsonPath("$.data.totalRatio").value(200))
			.andExpect(jsonPath("$.data.aweekAgoTotal").value(100000));
	}

	/*
		사용자의 오늘 지출 총액을 다른 사람들의 지출의 평균값과 비교한 결과를 반환합니다.
 	*/
	@Test
	@DisplayName("GET /api/v1/expenditure/statistics/otheruser 는 다른 사람들의 지출 평균과 나이 지출을 비교합니다.")
	void t14() throws Exception {
		// "user3"에 해당하는 사용자 정보를 로드하고 JWT 토큰 생성
		Member member = memberService.get("user1");
		token = jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
		// When
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/expenditure/statistics/otheruser")
					.header("Authorization", "Bearer " + token) // 생성한 토큰을 헤더에 포함
			)
			.andDo(print());

		// Then
		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.resultCode").value("S-1"))
			.andExpect(jsonPath("$.msg").value("다른 사람들 평균 지출 대비 나의 지출 비율 반환 성공"))
			.andExpect(jsonPath("$.data.totalPrice").value(200000))
			.andExpect(jsonPath("$.data.othersAverage").value(37058))
			.andExpect(jsonPath("$.data.expenditureRatio").value(539.7));
	}
}
