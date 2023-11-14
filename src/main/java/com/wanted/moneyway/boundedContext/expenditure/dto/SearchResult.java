package com.wanted.moneyway.boundedContext.expenditure.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.wanted.moneyway.boundedContext.expenditure.entity.Expenditure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {
	private Integer totalSpending;
	private List<CategorySum> spendingByCategory;
	private Page<Expenditure> expenditures;
}
