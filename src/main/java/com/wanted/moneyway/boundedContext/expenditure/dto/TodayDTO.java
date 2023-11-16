package com.wanted.moneyway.boundedContext.expenditure.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodayDTO {
	private Integer recommendTodayTotalPrice;
	private Integer todayTotalPrice;
	private List<CategorySumWithDanger> categorySumWithDanger;
}
