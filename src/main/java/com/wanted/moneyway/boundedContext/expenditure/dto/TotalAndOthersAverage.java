package com.wanted.moneyway.boundedContext.expenditure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TotalAndOthersAverage {
	private Integer totalPrice;
	private Integer othersAverage;
}
