package com.wanted.moneyway.boundedContext.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanDTO {
	private int food = 0;
	private int cafe = 0;
	private int education = 0;
	private int dwelling = 0; // 주거비
	private int communication = 0; // 통신비
	private int shopping = 0;
	private int transfer = 0;
	private int others = 0;

	// 하나라도 값이 있어야 예산 설정 가능
	public boolean checkAllZero() {
		return food == 0 && cafe == 0 && education == 0 && dwelling == 0 &&
			communication == 0 && shopping == 0 && transfer == 0 && others == 0;
	}
}
