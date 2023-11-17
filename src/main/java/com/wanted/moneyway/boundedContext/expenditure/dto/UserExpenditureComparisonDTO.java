package com.wanted.moneyway.boundedContext.expenditure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExpenditureComparisonDTO {
	private Integer totalPrice;
	private Integer othersAverage;
	private Double expenditureRatio;
}
