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
import com.wanted.moneyway.boundedContext.expenditure.dto.AMothAgoRatioResult;
import com.wanted.moneyway.boundedContext.expenditure.dto.AWeekAgoRatioRequest;
import com.wanted.moneyway.boundedContext.expenditure.dto.BudgetCalculationResult;
import com.wanted.moneyway.boundedContext.expenditure.dto.CategoryRatio;
import com.wanted.moneyway.boundedContext.expenditure.dto.CategorySum;
import com.wanted.moneyway.boundedContext.expenditure.dto.CategorySumWithDanger;
import com.wanted.moneyway.boundedContext.expenditure.dto.ExpenditureDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.RecommendDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.RemainingDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.SearchRequestDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.SearchResult;
import com.wanted.moneyway.boundedContext.expenditure.dto.TodayDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.TotalAndCategorySumDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.TotalAndOthersAverage;
import com.wanted.moneyway.boundedContext.expenditure.dto.UserExpenditureComparisonDTO;
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
	private BudgetCalculationResult calculateBudget(List<Plan> plans, TotalAndCategorySumDTO totalAndCategorySum,
		int remainingDays) {
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

	/*
		오늘 지출 내용을 총액과 카테고리별 금액으로 알려주는 메서드
		컨설팅의 일부로 아래 내용을 추가로 제공
		- 오늘 적절 금액 : 오늘 사용했으면 적절했을 금액
		- 오늘 지출 금액 : 오늘 기준 사용한 총 금액
		- 위험도 : 카테고리별 적정 금액, 지출금액의 차이를 위험도로 나타냄
	 */
	public RsData getToday(String userName) {
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
		SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
		searchRequestDTO.setStartDate(startOfMonth);
		searchRequestDTO.setEndDate(today.minusDays(1)); // 어제 날짜까지 구해야 오늘 추천액 계산

		// 지출에서 이번달 지출액 구하기
		// 어제 날짜까지 지출액 총합 / 카테고리별 금액이 나옴
		TotalAndCategorySumDTO totalBeforeDayAndCategorySum = expenditureRepository.getTotalAndCategorySum(member,
			searchRequestDTO);

		// 오늘 사용한 금액을 구하기 위한 DTO 객체 생성
		SearchRequestDTO todaySearchRequestDTO = SearchRequestDTO
			.builder()
			.startDate(LocalDate.now())
			.endDate(LocalDate.now())
			.build();

		// 말일까지 남은 일수 계산
		int diffDays = Ut.date.getDaysBetweenDates(today, endOfMonth);

		// 예산 계획, 어제까지 지출액, 말일까지 남은 일 수로 예산 추천
		BudgetCalculationResult result = calculateBudget(data, totalBeforeDayAndCategorySum, diffDays);

		// 예산 추천 결과 추출
		Integer diffTotal = result.getDiffTotal(); // 전체 남은액
		List<CategorySum> diffCategorySumList = result.getDiffCategorySumList(); // 각 카테고리별 추천액
		Integer recommendTodayTotalPrice = result.getTodayTotal(); // 오늘 사용 가능한 총액 추천

		// 오늘 사용한 총액과 카테고리별 금액
		TotalAndCategorySumDTO todayTotalAndCategorySum = expenditureRepository.getTotalAndCategorySum(member,
			todaySearchRequestDTO);

		// 결과용
		Integer todayTotalPrice = todayTotalAndCategorySum.getTotalSpending(); // 오늘 사용한 총액
		List<CategorySum> todayPriceEachCategory = todayTotalAndCategorySum.getCategorySumList();

		// 각각 실제 금액과 위험도 계산
		// 반환 형태
		// 1. 오늘 추천 총액 :  recommendTodayTotalPrice
		// 2. 실제 사용한 오늘 총액 : todayTotalPrice
		// 3. 각 카테고리별 실제 사용금액 및 위험도 : 구해야 함

		List<CategorySumWithDanger> todayResultWithDangerList = new ArrayList<>();

		for (CategorySum c : diffCategorySumList) {
			// 오늘 추천한 카테고리를 지출했는지 검사
			Optional<CategorySum> optionalCategorySum = todayPriceEachCategory.stream()
				.filter(categorySum -> c.getCategoryId().equals(categorySum.getCategoryId()))
				.findFirst();

			CategorySumWithDanger categorySumWithDanger;
			// 지출 내역에 해당 카테고리가 있는 경우
			if (optionalCategorySum.isPresent()) {
				CategorySum categorySum = optionalCategorySum.get();
				categorySumWithDanger = CategorySumWithDanger.builder()
					.categoryId(categorySum.getCategoryId())
					.categoryName(categorySum.getCategoryName())
					.recommendTodaySpending(c.getSpending())  // 추천 금액
					.todaySpending(categorySum.getSpending())  // 실제 사용금액
					.danger(Math.round(((double)categorySum.getSpending() / c.getSpending()) * 1000)
						/ 10.0) // 위험도 : 사용액 / 추천액 2번째 자리에서 반올림
					.build();
			}
			// 지출 내역에 해당 카테고리가 없는 경우
			else {
				categorySumWithDanger = CategorySumWithDanger.builder()
					.categoryId(c.getCategoryId())
					.categoryName(c.getCategoryName())
					.recommendTodaySpending(c.getSpending())  // 추천 금액
					.todaySpending(0)  // 실제 사용금액
					.danger(0.0) // 사용액이 없으니 0
					.build();
			}

			todayResultWithDangerList.add(categorySumWithDanger);
		}

		// DTO 객체
		TodayDTO todayDTO = TodayDTO.builder()
			.recommendTodayTotalPrice(recommendTodayTotalPrice)
			.todayTotalPrice(todayTotalPrice)
			.categorySumWithDanger(todayResultWithDangerList)
			.build();

		return RsData.of("S-1", "오늘 지출 내역과 위험도 반환 성공", todayDTO);
	}

	/*
		오늘 지출 내역을 지난달 대비 총액, 카테고리별 소비율을 반환하는 메서드
		- 오늘 날짜 지출 총액 vs 지난달의 오늘 일자 지출 총액 비율
		- 오늘 날짜 카테고리별 지출 총액 vs 지난달의 오늘 일자 카테고리별 지출 총액 비율
 	*/
	public RsData getLastMonthStatisics(String userName) {
		Member member = memberService.get(userName);

		// 오늘 날짜에서 1달 뺀 날짜 추출
		LocalDate targetDate = LocalDate.now().minusMonths(1);

		// 시작일 추출
		LocalDate startTargetMonth = Ut.date.getStartOfMonth(targetDate);

		// 한달전의 (1일~오늘 일자) 지출 추출용 DTO
		SearchRequestDTO searchRequestDTO = SearchRequestDTO.builder()
			.endDate(targetDate)
			.startDate(startTargetMonth)
			.build();

		TotalAndCategorySumDTO totalAndCategorySum = expenditureRepository.getTotalAndCategorySum(member,
			searchRequestDTO);

		Integer aMonthAgoTotalPrice = totalAndCategorySum.getTotalSpending();
		List<CategorySum> aMonthAgoCategorySumPrice = totalAndCategorySum.getCategorySumList();

		// 오늘 날짜
		LocalDate today = LocalDate.now();

		// 이번달 시작일 추출
		LocalDate startThisMonth = Ut.date.getStartOfMonth(today);

		// 이번달 (1일 ~ 오늘 일자) 지출 -> 추출용 DTO 수정
		searchRequestDTO = SearchRequestDTO.builder()
			.startDate(startThisMonth)
			.endDate(today)
			.build();

		// 이번달 지출 추출
		totalAndCategorySum = expenditureRepository.getTotalAndCategorySum(member, searchRequestDTO);

		Integer currentTotalPrice = totalAndCategorySum.getTotalSpending();
		List<CategorySum> currentCategorySumPrice = totalAndCategorySum.getCategorySumList();

		/*
			결과 반환용 Data
			- 이번달(1~현재 일자) 사용금액
			- 저번달(1~현재 일자) 사용금액
			- 이번달 / 저번달 전체 금액 비율
			- 총 사용량 비교 (현재 사용량 / 지난달 오늘 일자까지 사용량) : totalRatio
			- 각 카테고리별 사용량 비교 (카테고리별 현재 사용량 / 지난달 오늘 일자까지 사용량) : categoryRatioList
		 */

		// 총 사용량 비교 2번째 자리에서 반올림
		Double totalRatio = Math.round((double)currentTotalPrice / aMonthAgoTotalPrice * 100 * 10) / 10.0;

		List<CategoryRatio> categoryRatioList = new ArrayList<>();
		// 이번달 지출 카테고리거 더 많은 경우
		// 이번달 지출 카테고리를 하나씩 다 검사
		if (currentCategorySumPrice.size() >= aMonthAgoCategorySumPrice.size()) {
			for (CategorySum c : currentCategorySumPrice) {
				// 이번달 지출한 카테고리를 저번달에도 지출했는지 검사
				Optional<CategorySum> optionalCategorySum = aMonthAgoCategorySumPrice.stream()
					.filter(aMonthAgoCategorySum -> c.getCategoryId().equals(aMonthAgoCategorySum.getCategoryId()))
					.findFirst();

				CategoryRatio categoryRatio;
				// 지출 내역에 해당 카테고리가 있는 경우
				if (optionalCategorySum.isPresent()) {
					CategorySum categorySum = optionalCategorySum.get();
					categoryRatio = CategoryRatio.builder()
						.categoryId(categorySum.getCategoryId())
						.categoryName(categorySum.getCategoryName())
						.aMonthAgoSpending(categorySum.getSpending())  // 지난달 사용 금액
						.todaySpending(c.getSpending())  // 오늘 사용금액
						.compareRatio(Math.round(((double)c.getSpending() / categorySum.getSpending()) * 1000)
							/ 10.0) // 비율 : 이번달 오늘 까지 사용액 / 저번달 오늘 날짜까지 사용액 2번째 자리에서 반올림
						.build();
				}
				// 지출 내역에 해당 카테고리가 없는 경우
				else {
					categoryRatio = CategoryRatio.builder()
						.categoryId(c.getCategoryId())
						.categoryName(c.getCategoryName())
						.aMonthAgoSpending(0)  // 지난달 사용액
						.todaySpending(c.getSpending())  // 이번달 사용금액
						.compareRatio(null) // 지난달 0원이기에 비율 계산 불가
						.build();
				}

				categoryRatioList.add(categoryRatio);
			}
		}

		// 저번달 지출 카테고리가 더 많은 경우
		// 저번달 지출 카테고리를 하나씩 다 검사
		else {
			for (CategorySum c : aMonthAgoCategorySumPrice) {
				// 이번달 지출한 카테고리를 저번달에도 지출했는지 검사
				Optional<CategorySum> optionalCategorySum = currentCategorySumPrice.stream()
					.filter(aMonthAgoCategorySum -> c.getCategoryId().equals(aMonthAgoCategorySum.getCategoryId()))
					.findFirst();

				CategoryRatio categoryRatio;
				// 저번달 지출 내역에 이번달 카테고리가 있는 경우
				if (optionalCategorySum.isPresent()) {
					CategorySum categorySum = optionalCategorySum.get();
					categoryRatio = CategoryRatio.builder()
						.categoryId(categorySum.getCategoryId())
						.categoryName(categorySum.getCategoryName())
						.aMonthAgoSpending(categorySum.getSpending())  // 지난달 사용 금액
						.todaySpending(c.getSpending())  // 오늘 사용금액
						.compareRatio(Math.round(((double)c.getSpending() / categorySum.getSpending()) * 1000)
							/ 10.0) // 비율 : 이번달 오늘 까지 사용액 / 저번달 오늘 날짜까지 사용액 2번째 자리에서 반올림
						.build();
				}
				// 지출 내역에 해당 카테고리가 없는 경우
				else {
					categoryRatio = CategoryRatio.builder()
						.categoryId(c.getCategoryId())
						.categoryName(c.getCategoryName())
						.aMonthAgoSpending(0)  // 지난달 사용액
						.todaySpending(c.getSpending())  // 이번달 사용금액
						.compareRatio(null) // 지난달 0원이기에 비율 계산 불가
						.build();
				}

				categoryRatioList.add(categoryRatio);
			}
		}

		AMothAgoRatioResult result = AMothAgoRatioResult.builder()
			.currentTotal(currentTotalPrice)
			.aMonthAgoTotal(aMonthAgoTotalPrice)
			.totalRatio(totalRatio)
			.categoryRatioList(categoryRatioList)
			.build();

		return RsData.of("S-1", "지난달 대비 오늘 일자 기준 사용량 통계 데이터 추출 성공", result);
	}

	public RsData getLastWeekStatisics(String userName) {
		Member member = memberService.get(userName);

		// 오늘 날짜에서 7일 뺀 날짜 추출
		LocalDate targetDate = LocalDate.now().minusDays(7);

		// 한달전의 (1일~오늘 일자) 지출 추출용 DTO
		SearchRequestDTO searchRequestDTO = SearchRequestDTO.builder()
			.endDate(targetDate)
			.startDate(targetDate)
			.build();

		// 7일전 지출 데이터
		TotalAndCategorySumDTO totalAndCategorySum = expenditureRepository.getTotalAndCategorySum(member,
			searchRequestDTO);

		Integer aWeekAgoTotalPrice = totalAndCategorySum.getTotalSpending();

		// 오늘 날짜
		LocalDate today = LocalDate.now();

		// 이번달 (1일 ~ 오늘 일자) 지출 -> 추출용 DTO 수정
		searchRequestDTO = SearchRequestDTO.builder()
			.startDate(today)
			.endDate(today)
			.build();

		// 오늘 지출 추출
		totalAndCategorySum = expenditureRepository.getTotalAndCategorySum(member, searchRequestDTO);
		Integer todayTotalPrice = totalAndCategorySum.getTotalSpending();

		AWeekAgoRatioRequest result = AWeekAgoRatioRequest.builder()
			.todayTotal(todayTotalPrice)
			.aWeekAgoTotal(aWeekAgoTotalPrice)
			.totalRatio(Math.round(((double)todayTotalPrice / aWeekAgoTotalPrice) * 1000) / 10.0)
			.build();

		return RsData.of("S-1", "지난 요일과 총액 비교 성공", result);

	}

	public RsData getOtherUserStatisics(String userName) {
		Member member = memberService.get(userName);

		// 오늘 날짜
		LocalDate today = LocalDate.now();

		// 오늘 날짜로 데이터 구할 DTO 객체 생성
		SearchRequestDTO searchRequestDTO = SearchRequestDTO.builder()
			.endDate(today)
			.startDate(today)
			.build();

		TotalAndOthersAverage totalAndOthersAverage = expenditureRepository.getTotalAndOthersAverage(member,
			searchRequestDTO);

		Integer userTotal = totalAndOthersAverage.getTotalPrice();
		Integer otherTotalAvg = totalAndOthersAverage.getOthersAverage();

		UserExpenditureComparisonDTO result = UserExpenditureComparisonDTO
			.builder()
			.othersAverage(otherTotalAvg)
			.totalPrice(userTotal)
			// 비율 : 나의 지출액 / 다른 사람들 평균 지출액 2번째 자리에서 반올림
			.expenditureRatio((Math.round(((double)userTotal / otherTotalAvg) * 1000) / 10.0))
			.build();

		return RsData.of("S-1", "다른 사람들 평균 지출 대비 나의 지출 비율 반환 성공", result);
	}
}
