package com.wanted.moneyway.boundedContext.expenditure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRatio {
	private Long categoryId;
	private String categoryName;
	private Integer aMonthAgoSpending;
	private Integer todaySpending;
	private Double compareRatio;
}
