package com.wanted.moneyway.boundedContext.expenditure.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.category.entity.Category;
import com.wanted.moneyway.boundedContext.category.service.CategoryService;
import com.wanted.moneyway.boundedContext.expenditure.dto.ExpenditureDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.SearchRequestDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.SearchResult;
import com.wanted.moneyway.boundedContext.expenditure.dto.TotalAndCategorySumDTO;
import com.wanted.moneyway.boundedContext.expenditure.entity.Expenditure;
import com.wanted.moneyway.boundedContext.expenditure.repository.ExpenditureRepository;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenditureService {

	private final ExpenditureRepository expenditureRepository;
	private final MemberService memberService;

	private final CategoryService categoryService;

	@Transactional
	public RsData<Expenditure> create(ExpenditureDTO expenditureDTO, String username) {
		Member member = memberService.get(username);

		Category category = categoryService.get(expenditureDTO.getCategoryId());

		if(category == null) {
			return RsData.of("F-1", "해당 카테고리는 존재하지 않습니다.");
		}

		Expenditure expenditure = Expenditure.builder()
			.memo(expenditureDTO.getMemo())
			.isTotal(expenditureDTO.getIsTotal())
			.spendDate(expenditureDTO.getSpendDate())
			.category(category)
			.spendingPrice(expenditureDTO.getSpendingPrice())
			.member(member)
			.build();

		expenditureRepository.save(expenditure);

		return RsData.of("S-1", "지출 정보 저장 성공", expenditure);

	}

	@Transactional
	public RsData delete(Long expenditureId, String username) {
		Member member = memberService.get(username);

		Expenditure expenditure = expenditureRepository.findById(expenditureId).orElse(null);

		if(expenditure == null)
			return RsData.of("F-1", "이미 삭제되었거나 존재하지 않는 내역입니다.");
		if(!expenditure.getMember().equals(member))
			return RsData.of("F-1", "내역 작성한 사용자만 삭제 가능합니다.");

		expenditureRepository.delete(expenditure);

		return RsData.of("S-1", "삭제 성공");
	}
/*
	userName과 expeditureId를 받아 지출 내역을 반환하는 메서드
 */
	public RsData<Expenditure> search(String userName, Long expenditureId) {
		Member member = memberService.get(userName);

		Expenditure expenditure = expenditureRepository.findById(expenditureId).orElse(null);

		if(expenditure == null)
			return RsData.of("F-1", "이미 삭제되었거나 존재하지 않는 내역입니다.");

		if(!expenditure.getMember().equals(member))
			return RsData.of("F-1", "지출 내역 작성자가 아닙니다.");

		return RsData.of("S-1", "내역 조회 성공", expenditure);
	}

	public RsData search(String userName, SearchRequestDTO searchRequestDTO) {
		Member member = memberService.get(userName);

		Page<Expenditure> expenditurePage = expenditureRepository.searchExpenditure(member, searchRequestDTO);
		TotalAndCategorySumDTO totalAndCategorySum = expenditureRepository.getTotalAndCategorySum(member,
			searchRequestDTO);

		SearchResult searchResult = new SearchResult(totalAndCategorySum.getTotalSpending(),
			totalAndCategorySum.getCategorySumList(), expenditurePage);

		return RsData.of("S-1", "조회 성공", searchResult);
	}
}
