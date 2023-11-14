package com.wanted.moneyway.boundedContext.expenditure.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.expenditure.dto.ExpenditureDTO;
import com.wanted.moneyway.boundedContext.expenditure.entity.Expenditure;
import com.wanted.moneyway.boundedContext.expenditure.service.ExpenditureService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/expenditure")
@Tag(name = "ExpenditureController", description = "지출 컨트롤러")
@SecurityRequirements({
	@SecurityRequirement(name = "bearerAuth"),
	@SecurityRequirement(name = "RefreshToken")
})
public class ApiV1ExpenditureController {

	private final ExpenditureService expenditureService;

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
	@Operation(summary = "지출 내역 저장")
	public RsData create(@RequestBody ExpenditureDTO expenditureDTO, @AuthenticationPrincipal User user) {
		RsData<Expenditure> rsCreate = expenditureService.create(expenditureDTO, user.getUsername());
		if (rsCreate.isFail())
			return rsCreate;

		return RsData.of(rsCreate.getResultCode(), rsCreate.getMsg(), ExpenditureDTO.of(rsCreate.getData()));
	}
}
