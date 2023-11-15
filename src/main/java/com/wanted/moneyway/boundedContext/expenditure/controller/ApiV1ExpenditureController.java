package com.wanted.moneyway.boundedContext.expenditure.controller;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.expenditure.dto.ExpenditureDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.SearchRequestDTO;
import com.wanted.moneyway.boundedContext.expenditure.dto.SearchResult;
import com.wanted.moneyway.boundedContext.expenditure.entity.Expenditure;
import com.wanted.moneyway.boundedContext.expenditure.service.ExpenditureService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
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
	@PostMapping("")
	@Operation(summary = "지출 내역 저장")
	public RsData create(@RequestBody ExpenditureDTO expenditureDTO, @AuthenticationPrincipal User user) {
		RsData<Expenditure> rsCreate = expenditureService.create(expenditureDTO, user.getUsername());
		if (rsCreate.isFail())
			return rsCreate;

		return RsData.of(rsCreate.getResultCode(), rsCreate.getMsg(), ExpenditureDTO.of(rsCreate.getData()));
	}

	@Data
	public static class DeleteRequest {
		@NotBlank(message = "삭제 지출 내역 id를 입력해주세요")
		@Schema(description = "지출 내역 id", example = "1")
		private Long expenditureId;
	}

	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("")
	@Operation(summary = "지출 내역 삭제")
	public RsData delete(@RequestBody DeleteRequest deleteRequest, @AuthenticationPrincipal User user) {
		RsData rsDelete = expenditureService.delete(deleteRequest.getExpenditureId(), user.getUsername());

		return rsDelete;
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{id}")
	@Operation(summary = "상세 지출 내역 조회")
	public RsData expenditures(@AuthenticationPrincipal User user, @PathVariable Long id) {
		RsData<Expenditure> rsRead = expenditureService.search(user.getUsername(), id);
		if (rsRead.isFail())
			return rsRead;

		return RsData.of(rsRead.getResultCode(), rsRead.getMsg(), ExpenditureDTO.of(rsRead.getData()));
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("")
	@Operation(summary = "지출 내역 목록 조회")
	public RsData<SearchResult> search(
		@RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now().minusDays(7)}") LocalDate startDate,
		@RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate endDate,
		@RequestParam(required = false) Long categoryId,
		@RequestParam(required = false) Integer minPrice,
		@RequestParam(required = false) Integer maxPrice,
		@RequestParam(required = false, defaultValue = "0") Integer pageNumber,
		@RequestParam(required = false, defaultValue = "10") Integer pageLimit,
		@AuthenticationPrincipal User user) {

		SearchRequestDTO searchRequestDTO = SearchRequestDTO
			.builder()
			.startDate(startDate)
			.endDate(endDate)
			.categoryId(categoryId)
			.minPrice(minPrice)
			.maxPrice(maxPrice)
			.pageLimit(pageLimit)
			.pageNumber(pageNumber)
			.build();

		System.out.println(searchRequestDTO);
		RsData<SearchResult> rsSearch = expenditureService.search(user.getUsername(), searchRequestDTO);
		return rsSearch;
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/remaining")
	@Operation(summary = "이번달 남은 예산 조회(총액 + 카테고리별)")
	public RsData remainingBudget(@AuthenticationPrincipal User user) {
		RsData rsRemaining = expenditureService.reaminingBudget(user.getUsername());
		return rsRemaining;
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/recommend")
	@Operation(summary = "오늘의 지출 금액 추천 API")
	public RsData recommendBudget(@AuthenticationPrincipal User user) {
		RsData rsRecommend = expenditureService.recommendBudget(user.getUsername());
		return rsRecommend;
	}

	@PreAuthorize("isAuthenticated()")
	@PatchMapping ("/{id}")
	@Operation(summary = "지출 내역 수정 API")
	public RsData modify(@AuthenticationPrincipal User user, @RequestBody ExpenditureDTO expenditureDTO, @PathVariable Long id) {
		RsData rsModify = expenditureService.modifyExpenditure(user.getUsername(), expenditureDTO, id);
		return rsModify;
	}
}
