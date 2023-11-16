package com.wanted.moneyway.boundedContext.expenditure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorySumWithDanger {
	private Long categoryId;
	private String categoryName;
	private Integer todaySpending;
	private Integer recommendTodaySpending;
	private Double danger;
}
