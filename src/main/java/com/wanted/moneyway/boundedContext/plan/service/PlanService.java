package com.wanted.moneyway.boundedContext.plan.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.category.entity.Category;
import com.wanted.moneyway.boundedContext.category.service.CategoryService;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.service.MemberService;
import com.wanted.moneyway.boundedContext.plan.dto.PlanDTO;
import com.wanted.moneyway.boundedContext.plan.dto.PlanPercentDTO;
import com.wanted.moneyway.boundedContext.plan.entity.Plan;
import com.wanted.moneyway.boundedContext.plan.repository.PlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanService {

	private final MemberService memberService;
	private final CategoryService categoryService;
	private final PlanRepository planRepository;

	@Transactional
	public RsData<List<Plan>> register(PlanDTO planDTO, String username) {
		RsData<List<Category>> searchCategoryRs = categoryService.getAll();
		// 카테고리가 없으면 실패 처리
		if(searchCategoryRs.isFail())
			return (RsData) searchCategoryRs;
		List<Category> categoryList = searchCategoryRs.getData();

		// 기존에 등록한 계획이 있는지 추출
		Member member = memberService.get(username);
		List<Plan> plans = planRepository.findAllByMember(member);
		// 있다면 수정 상태 flag 변수
		boolean isModify = plans.size() > 0;

		// Plan 객체에 카테고리가 차지하는 비율 계산용 총합 데이터
		int sumBudget = planDTO.sumAllFields();

		List<Plan> personalPlanList = new ArrayList<>();

		for (Category c : categoryList) {
			// 카테고리에 맞는 지출 계획 객체 추출
			Plan p = findPlanByCategory(plans, c);
			// DTO에서 예산 추출
			int budget = getBudgetByCategoryName(planDTO, c.getNameE());
			// null인 경우는 신규로 객체 생성
			if (p == null) {
				p = Plan.builder()
					.category(c)
					.budget(budget)
					.member(member)
					.build();
			} else {
				p.update(budget);
			}
			// 신규건 수정이건 비율 업데이트 공통이라 분기문 밖으로 뺌
			p.updateCategoryRatio((double) budget / sumBudget);
			personalPlanList.add(p);
		}

		planRepository.saveAll(personalPlanList);

		return RsData.of("S-1", isModify ? "지출 계획 수정 완료" : "지출 계획 생성 완료", personalPlanList);
	}

	/*
		지출 계획에서 카테고리에 맞는 Plan 찾아주는 메서드
	 */
	private Plan findPlanByCategory(List<Plan> plans, Category c) {
		return plans.stream()
			.filter(p -> p.getCategory().equals(c))
			.findAny()
			.orElse(null);
	}

	/*
		DTO 객체에서 카테고리에 맞는 금액 추출 메서드
	 */
	private int getBudgetByCategoryName(PlanDTO planDTO, String categoryName) {
		switch (categoryName) {
			case "cafe": return planDTO.getCafe();
			case "dwelling": return planDTO.getDwelling();
			case "food": return planDTO.getFood();
			case "communication": return planDTO.getCommunication();
			case "education": return planDTO.getEducation();
			case "shopping": return planDTO.getShopping();
			case "transfer": return planDTO.getTransfer();
			default: return planDTO.getOthers();
		}
	}

	/*
		예산 추천 메서드
	 */
	public PlanDTO recommend(Integer totalPrice) {
		PlanPercentDTO planPercentDTO = planRepository.recommendPercent();
		return createPlanDtoFromPercentDto(totalPrice, planPercentDTO);
	}

	/*
		비율에 맞게 추천 금액 계산 메서드
	 */
	private PlanDTO createPlanDtoFromPercentDto(Integer totalPrice, PlanPercentDTO planPercentDTO) {
		PlanDTO dto = new PlanDTO();
		dto.setCafe((int) Math.ceil(totalPrice * planPercentDTO.getCafe()));
		dto.setFood((int) Math.ceil(totalPrice * planPercentDTO.getFood()));
		dto.setOthers((int) Math.ceil(totalPrice * planPercentDTO.getOthers()));
		dto.setDwelling((int) Math.ceil(totalPrice * planPercentDTO.getDwelling()));
		dto.setEducation((int) Math.ceil(totalPrice * planPercentDTO.getEducation()));
		dto.setCommunication((int) Math.ceil(totalPrice * planPercentDTO.getCommunication()));
		dto.setShopping((int) Math.ceil(totalPrice * planPercentDTO.getShopping()));
		dto.setTransfer((int) Math.ceil(totalPrice * planPercentDTO.getTransfer()));
		return dto;
	}
}