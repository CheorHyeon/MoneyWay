package com.wanted.moneyway.boundedContext.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanPercentDTO {
	private Double food;
	private Double cafe;
	private Double education;
	private Double dwelling; // 주거비
	private Double communication; // 통신비
	private Double shopping;
	private Double transfer;
	private Double others;
}
