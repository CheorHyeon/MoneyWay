package com.wanted.moneyway.boundedContext.plan.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.member.controller.ApiV1MemberController;
import com.wanted.moneyway.boundedContext.plan.dto.PlanDTO;
import com.wanted.moneyway.boundedContext.plan.entity.Plan;
import com.wanted.moneyway.boundedContext.plan.service.PlanService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/plan")
@Tag(name = "PlanController", description = "예산 계획 수립, 추천 컨트롤러")
public class ApiV1PlanController {

	private final PlanService planService;

	@PreAuthorize("isAuthenticated()")
	@PostMapping("")
	public RsData<List<Plan>> create(@RequestBody PlanDTO planDTO, @AuthenticationPrincipal User user) {
		System.out.println(planDTO.toString());
		if(planDTO.checkAllZero())
			return RsData.of("F-1", "예산 항목 하나라도 입력 해야 등록 가능합니다.");

		RsData<List<Plan>> rsData = planService.register(planDTO, user.getUsername());

		return rsData;
	}
}
