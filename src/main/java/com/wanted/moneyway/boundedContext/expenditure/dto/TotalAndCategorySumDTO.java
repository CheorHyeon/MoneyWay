package com.wanted.moneyway.boundedContext.expenditure.dto;

import java.util.List;

public record TotalAndCategorySumDTO(
	Integer totalSpending,
	List<CategorySum> categorySumList
) {
}
