package com.wanted.moneyway.boundedContext.expenditure.repository;

import static com.wanted.moneyway.boundedContext.category.entity.QCategory.*;
import static com.wanted.moneyway.boundedContext.expenditure.entity.QExpenditure.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wanted.moneyway.boundedContext.expenditure.dto.CategorySum;
import com.wanted.moneyway.boundedContext.expenditure.dto.SearchRequestDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.TotalAndCategorySumDTO;
import com.wanted.moneyway.boundedContext.expenditure.entity.Expenditure;
import com.wanted.moneyway.boundedContext.member.entity.Member;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomExpenditureRepositoryImpl implements CustomExpenditureRepository {
	private final JPAQueryFactory jpaQueryFactory;

	/*
		페이지별 지출 데이터 조회
	 */
	@Override
	public Page<Expenditure> searchExpenditure(Member member, SearchRequestDTO searchRequestDTO) {
		LocalDate startDate = searchRequestDTO.getStartDate();
		LocalDate endDate = searchRequestDTO.getEndDate();
		Long categoryId = searchRequestDTO.getCategoryId();
		Integer minPrice = searchRequestDTO.getMinPrice();
		Integer maxPrice = searchRequestDTO.getMaxPrice();

		Integer pageNumber = searchRequestDTO.getPageNumber();
		Integer pageLimit = searchRequestDTO.getPageLimit();

		Pageable pageable = PageRequest.of(pageNumber, pageLimit);

		// 여러 조건을 동적으로 추가할 수 있도록 도와주는 BooleanBuilder 도입
		BooleanBuilder builder = createBooleanBuilder(categoryId, minPrice, maxPrice);

		// 조건에 맞는 목록 구하기
		List<Expenditure> expenditures = jpaQueryFactory.selectFrom(expenditure)
			.where(
				expenditure.member.eq(member),
				expenditure.spendDate.between(startDate, endDate),
				builder // 조건 동적 추가
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// 전체 데이터 개수 추출
		long total = jpaQueryFactory.selectFrom(expenditure)
			.where(
				expenditure.member.eq(member),
				expenditure.spendDate.between(startDate, endDate),
				builder // 조건 동적 추가
			)
			.fetchCount();

		return new PageImpl<>(expenditures, pageable, total);
	}

	/*
		총 합계 금액, 각 카테고리별 합계 금액 추출
	 */
	@Override
	public TotalAndCategorySumDTO getTotalAndCategorySum(Member member, SearchRequestDTO searchRequestDTO) {
		LocalDate startDate = searchRequestDTO.getStartDate();
		LocalDate endDate = searchRequestDTO.getEndDate();
		Long categoryId = searchRequestDTO.getCategoryId();
		Integer minPrice = searchRequestDTO.getMinPrice();
		Integer maxPrice = searchRequestDTO.getMaxPrice();

		// 여러 조건을 동적으로 추가할 수 있도록 도와주는 BooleanBuilder 도입
		BooleanBuilder builder = createBooleanBuilder(categoryId, minPrice, maxPrice);

		// 조건에 맞는 지출 리스트 추출
		List<Expenditure> expenditures = jpaQueryFactory.selectFrom(expenditure)
			.where(
				expenditure.member.eq(member),
				expenditure.spendDate.between(startDate, endDate),
				expenditure.isTotal.eq(true),
				builder // 조건 동적 추가
			)
			.fetch();

		// 지출 합계 계산
		Integer totalSpending = 0;
		for (Expenditure expenditure : expenditures) {
			totalSpending += expenditure.getSpendingPrice();
		}

		// 카테고리별 지출 합계 추출용 map
		Map<Long, Integer> categorySums = new HashMap<>();
		for (Expenditure expenditure : expenditures) {
			Long targetCategoryId = expenditure.getCategory().getId();
			categorySums.put(targetCategoryId,
				categorySums.getOrDefault(targetCategoryId, 0) + expenditure.getSpendingPrice());
		}

		List<CategorySum> categorySumList = categorySums.entrySet().stream()
			.map(entry -> new CategorySum(
				entry.getKey(), // id 추출
				getCategoryNameH(entry.getKey()), // 이름 추출
				entry.getValue() // 합계 추출
			))
			.collect(Collectors.toList());

		return new TotalAndCategorySumDTO(totalSpending, categorySumList);
	}

	/*
		카테고리 한글 이름 추출 메서드
	 */
	private String getCategoryNameH(Long findCategoryId) {
		return jpaQueryFactory
			.select(category.nameH)
			.from(category)
			.where(category.id.eq(findCategoryId))
			.fetchOne();
	}

	private BooleanBuilder createBooleanBuilder(Long categoryId, Integer minPrice, Integer maxPrice) {
		BooleanBuilder builder = new BooleanBuilder();

		// 카테고리 입력 시에만 조건 적용되도록
		if (categoryId != null) {
			builder.and(expenditure.category.id.eq(categoryId));
		}

		// case 1. 최소, 최대 금액 범위가 입력된 경우 // 이 경우 minPrice ~ maxPrice 조건 추가
		if (minPrice != null && maxPrice != null) {
			builder.and(expenditure.spendingPrice.between(minPrice, maxPrice));
		}
		// case 2. 최소 금액 범위만 입력된 경우 // 이 경우 minPrice 이상인 조건 추가
		else if (minPrice != null) {
			builder.and(expenditure.spendingPrice.goe(minPrice));
		}
		// case 3. 최대 금액 범위만 입력된 경우 -> 이 경우 maxPrice 이하인 조건 추가
		else if (maxPrice != null) {
			builder.and(expenditure.spendingPrice.loe(maxPrice));
		}

		return builder;
	}
}
