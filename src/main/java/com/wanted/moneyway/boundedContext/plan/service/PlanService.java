package com.wanted.moneyway.boundedContext.plan.service;

import static com.wanted.moneyway.boundedContext.plan.entity.QPlan.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.Tuple;
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

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PlanService {

	private final MemberService memberService;

	private final CategoryService categoryService;

	private final PlanRepository planRepository;

	@Transactional
	public RsData<List<Plan>> register(PlanDTO planDTO, String username) {
		Member member = memberService.get(username);

		RsData<List<Category>> searchCategoryRs = categoryService.getAll();

		int cafe = planDTO.getCafe();
		int dwelling = planDTO.getDwelling();
		int food = planDTO.getFood();
		int communication = planDTO.getCommunication();
		int education = planDTO.getEducation();
		int shopping = planDTO.getShopping();
		int transfer = planDTO.getTransfer();
		int others = planDTO.getOthers();

		int sumBuget = cafe + dwelling + food + communication + education +shopping + transfer + others;

		if(searchCategoryRs.isFail())
			return (RsData) searchCategoryRs;

		List<Category> categoryList = searchCategoryRs.getData();

		List<Plan> personalPlanList = new ArrayList<>();

		for(Category c : categoryList) {
			int budget = 0;

			switch (c.getNameE()) {
				case "cafe" -> budget = cafe;
				case "dwelling" -> budget = dwelling;
				case "food" -> budget = food;
				case "communication" -> budget = communication;
				case "education" -> budget = education;
				case "shopping" -> budget = shopping;
				case "transfer" -> budget = transfer;
				default -> budget = others;
			}

			Plan p = Plan.builder()
				.category(c)
				.budget(budget)
				.member(member)
				.categoryRatio((double)budget/sumBuget)
				.build();

			personalPlanList.add(p);
		}

		planRepository.saveAll(personalPlanList);

		return RsData.of("S-1", "지출 계획 생성 완료", personalPlanList);
	}

	public PlanDTO recommend(Integer totalPrice) {
		PlanPercentDTO planPercentDTO = planRepository.recommendPercent();

		Integer food = (int) Math.ceil(totalPrice * planPercentDTO.getFood());
		Integer cafe = (int) Math.ceil(totalPrice * planPercentDTO.getCafe());
		Integer education = (int) Math.ceil(totalPrice * planPercentDTO.getEducation());
		Integer dwelling = (int) Math.ceil(totalPrice * planPercentDTO.getDwelling());
		Integer communication = (int) Math.ceil(totalPrice * planPercentDTO.getCommunication());
		Integer shopping= (int) Math.ceil(totalPrice * planPercentDTO.getShopping());
		Integer transfer = (int) Math.ceil(totalPrice * planPercentDTO.getTransfer());
		Integer others = (int) Math.ceil(totalPrice * planPercentDTO.getOthers());

		// DTO 객체 생성
		PlanDTO dto = new PlanDTO();
		dto.setCafe(cafe);
		dto.setFood(food);
		dto.setOthers(others);
		dto.setDwelling(dwelling);
		dto.setEducation(education);
		dto.setCommunication(communication);
		dto.setShopping(shopping);
		dto.setTransfer(transfer);

		return dto;
	}
}
