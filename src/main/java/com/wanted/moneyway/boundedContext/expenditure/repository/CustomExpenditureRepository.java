package com.wanted.moneyway.boundedContext.expenditure.repository;

import org.springframework.data.domain.Page;

import com.wanted.moneyway.boundedContext.expenditure.dto.SearchRequestDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.TotalAndCategorySumDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.TotalAndOthersAverage;
import com.wanted.moneyway.boundedContext.expenditure.entity.Expenditure;
import com.wanted.moneyway.boundedContext.member.entity.Member;

public interface CustomExpenditureRepository {
	Page<Expenditure> searchExpenditure(Member member, SearchRequestDTO searchRequestDTO);

	TotalAndCategorySumDTO getTotalAndCategorySum(Member member, SearchRequestDTO searchRequestDTO);

	TotalAndOthersAverage getTotalAndOthersAverage(Member member, SearchRequestDTO searchRequestDTO);
}
