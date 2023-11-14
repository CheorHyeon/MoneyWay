package com.wanted.moneyway.boundedContext.expenditure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategorySum {
	private Long categoryId;
	private String categoryName;
	private Integer spending;
}
