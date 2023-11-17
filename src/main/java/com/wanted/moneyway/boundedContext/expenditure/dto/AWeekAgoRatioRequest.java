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
public class AWeekAgoRatioRequest {
	private Integer todayTotal;
	private Integer aWeekAgoTotal;
	private Double totalRatio;
}
