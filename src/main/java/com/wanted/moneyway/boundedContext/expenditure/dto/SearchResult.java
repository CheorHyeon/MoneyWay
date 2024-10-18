package com.wanted.moneyway.boundedContext.expenditure.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;

@Builder
public record SearchResult(
	Integer totalSpending,
	List<CategorySum> spendingByCategory,
	Page<ExpenditureDTO> expenditures
) {
	public static SearchResult of(TotalAndCategorySumDTO totalAndCategorySum, Page<ExpenditureDTO> expenditurePage) {
		return SearchResult.builder()
			.totalSpending(totalAndCategorySum.totalSpending())
			.spendingByCategory(totalAndCategorySum.categorySumList())
			.expenditures(expenditurePage)
			.build();
	}
}
