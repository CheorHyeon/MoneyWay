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
import com.wanted.moneyway.boundedContext.expenditure.dto.BudgetCalculationResult;
import com.wanted.moneyway.boundedContext.expenditure.dto.CategorySum;
import com.wanted.moneyway.boundedContext.expenditure.dto.ExpenditureDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.RecommendDTO;
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

		if (category == null) {
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

		if (expenditure == null)
			return RsData.of("F-1", "이미 삭제되었거나 존재하지 않는 내역입니다.");
		if (!expenditure.getMember().equals(member))
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

		if (expenditure == null)
			return RsData.of("F-1", "이미 삭제되었거나 존재하지 않는 내역입니다.");

		if (!expenditure.getMember().equals(member))
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
		if (rsAllByMember.isFail())
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
		for (Plan p : data) {
			Optional<CategorySum> optionalCategorySum = categorySumList.stream()
				.filter(categorySum -> p.getCategory().getId().equals(categorySum.getCategoryId()))
				.findFirst();

			CategorySum diffCategorySum;
			// 지출 내역에 해당 카테고리가 있는 경우
			if (optionalCategorySum.isPresent()) {
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

	/*
		금일 사용가능한 예산을 추천해주는 메서드
		반환 형태
			1. 지출 목표량 남은 금액 : 목표 - 현재까지 사용한 총 금액
			2. 오늘 사용할 총액 : 지출 목표량 남은 금액 / 말일까지 남은 일
			3. 각 카테고리별 추천액 : 각 카테고리별 남은 금액 총액 / 말일까지 남은일
			4. 응원 메세지
			   - Case 1 : 지출 목표량 남은 금액 자체가 초과한 경우 0원으로 반환하고 메세지로 "목표한 예산을 초과하였으니 최대한 절약하며 하루를 보내봅시다!" 출력
			   - Case 2 : 각 카테고리별 추천액 중 초과한 예산이 있을 경우 "해당 카테고리 분야의 사용할 수 있는 예산이 없네요. 금일 사용할 총액 이내에서 충당해도 좋으니 절약하는 하루를 보내봅시다!" 출력
	 */
	public RsData recommendBudget(String userName) {
		Member member = memberService.get(userName);

		// 예산 계획 추출
		RsData<List<Plan>> rsAllByMember = planService.getAllByMember(member);
		if (rsAllByMember.isFail())
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
		// 1일 ~ 어제까지 날짜 사용한 데이터 추출
		SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
		searchRequestDTO.setStartDate(startOfMonth);
		searchRequestDTO.setEndDate(today.minusDays(1));

		// 지출에서 이번달 지출액 구하기
		// 어제 날짜까지 지출액 총합 / 카테고리별 금액이 나옴
		TotalAndCategorySumDTO totalAndCategorySum = expenditureRepository.getTotalAndCategorySum(member,
			searchRequestDTO);

		// 말일까지 남은 일수 계산
		int diffDays = Ut.date.getDaysBetweenDates(today, endOfMonth);

		// 예산 계획, 어제까지 지출액, 말일까지 남은 일 수로 예산 추천
		BudgetCalculationResult result = calculateBudget(data, totalAndCategorySum, diffDays);

		// 예산 추천 결과 추출
		Integer diffTotal = result.getDiffTotal();
		List<CategorySum> diffCategorySumList = result.getDiffCategorySumList();
		Integer todayTotal = result.getTodayTotal();

		// 4. 응원 메세지
		String message = null;
		// Case 1 : 지출 목표량 남은 금액 자체가 초과한 경우 0원으로 반환하고 메세지로 "목표한 예산을 초과하였으니 최대한 절약하며 하루를 보내봅시다!" 출력

		if (diffTotal <= 0) {
			message = "목표한 예산을 초과하였으니 최대한 절약하며 하루를 보내봅시다!";
		} else {
			// Case 2 : 각 카테고리별 추천액 중 초과한 예산이 있을 경우 "해당 카테고리 분야의 사용할 수 있는 예산이 없네요. 금일 사용할 총액 이내에서 충당해도 좋으니 절약하는 하루를 보내봅시다!" 출력
			for (CategorySum c : diffCategorySumList) {
				if (c.getSpending() <= 0) {
					message = "일부 카테고리 분야의 사용할 수 있는 예산이 없네요. 금일 사용할 총액 이내에서 충당해도 좋으니 절약하는 하루를 보내봅시다!";
					break;
				}
			}
		}

		if (message == null) {
			message = "절약하는 하루 보내세요!";
		}

		RecommendDTO recommendDTO = RecommendDTO.builder()
			.recommendPriceEachCategory(diffCategorySumList)
			.message(message)
			.todayTotalPrice(todayTotal)
			.totalRemainingPrice(diffTotal)
			.build();

		return RsData.of("S-1", "금일 지출액 추천 성공", recommendDTO);
	}

	// 예산 계획과 현재까지 지출액을 받아 추천 예산을 계산
	private BudgetCalculationResult calculateBudget(List<Plan> plans, TotalAndCategorySumDTO totalAndCategorySum, int remainingDays) {
		// 카테고리별 합계 금액
		List<CategorySum> categorySumList = totalAndCategorySum.getCategorySumList();
		List<CategorySum> diffCategorySumList = new ArrayList<>();

		// 실제 지출액 총 금액
		Integer expenditureTotal = totalAndCategorySum.getTotalSpending();

		// 지출 목표 총 사용 금액 추출
		Integer totalPrice = plans.stream()
			.mapToInt(a -> a.getBudget())
			.sum();

		// 지출 목표량 남은 금액 : 목표 - 현재까지 사용한 총 금액
		Integer diffTotal = totalPrice - expenditureTotal;

		// 오늘 사용할 총액 : 지출 목표량 남은 금액 / 말일까지 남은 일
		Integer todayTotal = diffTotal / remainingDays;

		// 각 카테고리별 추천액 : 각 카테고리별 남은 금액 총액 / 말일까지 남은일
		for (Plan p : plans) {
			Optional<CategorySum> optionalCategorySum = categorySumList.stream()
				.filter(categorySum -> p.getCategory().getId().equals(categorySum.getCategoryId()))
				.findFirst();

			CategorySum diffCategorySum;
			// 지출 내역에 해당 카테고리가 있는 경우
			if (optionalCategorySum.isPresent()) {
				CategorySum categorySum = optionalCategorySum.get();
				diffCategorySum = CategorySum.builder()
					.categoryId(p.getCategory().getId())
					.categoryName(p.getCategory().getNameH())
					.spending((p.getBudget() - categorySum.getSpending()) / remainingDays)
					.build();
			}
			// 지출 내역에 해당 카테고리가 없는 경우
			else {
				diffCategorySum = CategorySum.builder()
					.categoryId(p.getCategory().getId())
					.categoryName(p.getCategory().getNameH())
					.spending(p.getBudget() / remainingDays)
					.build();
			}

			diffCategorySumList.add(diffCategorySum);
		}

		return new BudgetCalculationResult(diffTotal, todayTotal, diffCategorySumList);
	}

	@Transactional
	public RsData modifyExpenditure(String userName, ExpenditureDTO expenditureDTO, Long expenditureId) {
		Member member = memberService.get(userName);

		Expenditure expenditure = expenditureRepository.findById(expenditureId).orElse(null);

		if (expenditure == null)
			return RsData.of("F-1", "존재하지 않는 지출 내역입니다.");

		if (!expenditure.getMember().equals(member))
			return RsData.of("F-1", "지출 내역 작성자만 수정 가능합니다.");

		Expenditure modifyExpenditure = expenditure.toBuilder()
			.category(expenditureDTO.getCategoryId() == null ? expenditure.getCategory() :
				categoryService.get(expenditureDTO.getCategoryId()))
			.isTotal(expenditureDTO.getIsTotal() == null ? expenditure.getIsTotal() : expenditureDTO.getIsTotal())
			.spendDate(
				expenditureDTO.getSpendDate() == null ? expenditure.getSpendDate() : expenditureDTO.getSpendDate())
			.memo(expenditureDTO.getMemo() == null ? expenditure.getMemo() : expenditureDTO.getMemo())
			.spendingPrice(expenditureDTO.getSpendingPrice() == null ? expenditure.getSpendingPrice() :
				expenditureDTO.getSpendingPrice())
			.build();

		expenditureRepository.save(modifyExpenditure);

		return RsData.of("S-1", "지출 내역 변경 성공", modifyExpenditure);
	}
}
