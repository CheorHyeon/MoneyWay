package com.wanted.moneyway.boundedContext.plan.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.plan.dto.PlanDTO;
import com.wanted.moneyway.boundedContext.plan.entity.Plan;
import com.wanted.moneyway.boundedContext.plan.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/plan")
@Tag(name = "PlanController", description = "예산 계획 수립, 추천 컨트롤러")
@SecurityRequirements({
	@SecurityRequirement(name = "bearerAuth"),
	@SecurityRequirement(name = "RefreshToken")
})
public class ApiV1PlanController {

	private final PlanService planService;

	@PreAuthorize("isAuthenticated()")
	@PostMapping("")
	@Operation(summary = "예산 계획 생성 및 수정 API")
	public RsData<List<Plan>> create(@RequestBody PlanDTO planDTO, @AuthenticationPrincipal User user) {
		if (planDTO.checkAllZero())
			return RsData.of("F-1", "예산 항목 하나라도 입력 해야 등록 가능합니다.");

		RsData<List<Plan>> rsData = planService.register(planDTO, user.getUsername());

		return rsData;
	}

	@Data
	public static class TotalPriceDTO {
		private Integer totalPrice;
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/recommend")
	@Operation(summary = "총 예산을 받아 카테고리별 예산 추천 API")
	public RsData<PlanDTO> recommend(@RequestBody TotalPriceDTO totalPriceDTO) {
		if (totalPriceDTO.getTotalPrice() == null || totalPriceDTO.getTotalPrice().equals(0)) {
			return RsData.of("F-1", "총 금액을 입력해주셔야 추천 가능합니다.");
		}
		PlanDTO dto = planService.recommend(totalPriceDTO.getTotalPrice());

		return RsData.of("S-1", "항목별 추천 금액", dto);
	}
}
