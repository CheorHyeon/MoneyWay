package com.wanted.moneyway.boundedContext.expenditure.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.category.entity.Category;
import com.wanted.moneyway.boundedContext.category.service.CategoryService;
import com.wanted.moneyway.boundedContext.expenditure.dto.CategorySum;
import com.wanted.moneyway.boundedContext.expenditure.dto.ExpenditureDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.RemainingDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.SearchRequestDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.SearchResult;
import com.wanted.moneyway.boundedContext.expenditure.dto.TotalAndCategorySumDTO;
import com.wanted.moneyway.boundedContext.expenditure.entity.Expenditure;
import com.wanted.moneyway.boundedContext.expenditure.repository.ExpenditureRepository;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.service.MemberService;
import com.wanted.moneyway.boundedContext.plan.entity.Plan;
import com.wanted.moneyway.boundedContext.plan.service.PlanService;
import com.wanted.moneyway.standard.util.Ut;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenditureService {

	private final ExpenditureRepository expenditureRepository;
	private final MemberService memberService;

	private final CategoryService categoryService;

	private final PlanService planService;

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

	/*
		남아있는 예산 조회 메서드
		1. 예산 지출 계획 추출
		2. 지출에서 이번달 지출액(총액, 카테고리별)의 합 구함
		3. 1(총액) - 2(총액) / 1(카테고리별 금액) - 2(이번달 카테고리별 지출 총 금액) 반환
	 */

	public RsData reaminingBudget(String userName) {
		Member member = memberService.get(userName);

		// 1. 예산 계획 추출
		RsData<List<Plan>> rsAllByMember = planService.getAllByMember(member);
		if(rsAllByMember.isFail())
			return rsAllByMember;

		List<Plan> data = rsAllByMember.getData();
		// 지출 목표 총 사용 금액 추출
		Integer totalPrice = data.stream()
			.mapToInt(a -> a.getBudget())
			.sum();

		LocalDate today = LocalDate.now();

		// 이번달 1일 추출
		LocalDate startOfMonth = Ut.date.getStartOfMonth(today);

		// 이번달 말일 추출
		LocalDate endOfMonth = Ut.date.getEndOfMonth(today);

		// 지출액 구할때 동적 쿼리 생성되지 않고 날짜만 적용되도록 설정용 DTO 객체 생성
		SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
		searchRequestDTO.setStartDate(startOfMonth);
		searchRequestDTO.setEndDate(endOfMonth);

		// 지출에서 이번달 지출액 구하기
		TotalAndCategorySumDTO totalAndCategorySum = expenditureRepository.getTotalAndCategorySum(member,
			searchRequestDTO);

		List<CategorySum> categorySumList = totalAndCategorySum.getCategorySumList();
		List<CategorySum> diffCategorySumList = new ArrayList<>();

		// 실제 지출액 총 금액
		Integer expenditureTotal = totalAndCategorySum.getTotalSpending();

		// 3-1. 목표액 - 실제 지출액 (남은 총 금액)
		Integer diffTotal = totalPrice - expenditureTotal;

		// 3-2. 카테고리별 사용하고 남은 금액 구하기
		for(Plan p : data) {
			Optional<CategorySum> optionalCategorySum = categorySumList.stream()
				.filter(categorySum -> p.getCategory().getId().equals(categorySum.getCategoryId()))
				.findFirst();

			CategorySum diffCategorySum;
			// 지출 내역에 해당 카테고리가 있는 경우
			if(optionalCategorySum.isPresent()) {
				CategorySum categorySum = optionalCategorySum.get();
				diffCategorySum = CategorySum
					.builder()
					.categoryId(p.getCategory().getId())
					.categoryName(p.getCategory().getNameH())
					.spending(p.getBudget() - categorySum.getSpending())
					.build();
			}
			// 지출 내역에 해당 카테고리가 없는 경우
			else {
				diffCategorySum = CategorySum
					.builder()
					.categoryId(p.getCategory().getId())
					.categoryName(p.getCategory().getNameH())
					.spending(p.getBudget())
					.build();
			}

			diffCategorySumList.add(diffCategorySum);
		}

		RemainingDTO result = RemainingDTO.builder()
			.totalRemainingPrice(diffTotal)
			.remainingPriceByCategoy(diffCategorySumList)
			.build();

		return RsData.of("S-1", "이번달 남은 지출액 조회 성공", result);

	}
}
