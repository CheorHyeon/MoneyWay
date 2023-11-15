package com.wanted.moneyway.boundedContext.expenditure.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDTO {
			// 1. 지출 목표량 남은 금액 : 목표 - 현재까지 사용한 총 금액
			// 2. 오늘 사용할 총액 : 지출 목표량 남은 금액 / 말일까지 남은 일
			// 3. 각 카테고리별 추천액 : 각 카테고리별 남은 금액 총액 / 말일까지 남은일
			// 4. 응원 메세지
	private Integer totalRemainingPrice;
	private Integer todayTotalPrice;
	private List<CategorySum> recommendPriceEachCategory;
	private String message;

}
