package com.wanted.moneyway.boundedContext.expenditure.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalAndCategorySumDTO {
	private Integer totalSpending;
	private List<CategorySum> categorySumList;
}
