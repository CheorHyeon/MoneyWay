package com.wanted.moneyway.boundedContext.expenditure.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchRequestDTO {

	@Schema(description = "조회 시작일을 입력해주세요(기본값 : 7일 전)", example = "2023-11-07")
	private LocalDate startDate = LocalDate.now().minusDays(7);
	@Schema(description = "조회 종료일을 입력해주세요(기본값 : 오늘)", example = "2023-11-14")
	private LocalDate endDate = LocalDate.now();

	@Schema(description = "조회 카테고리 Id를 입력해주세요(기본값 : 전체 데이터)", example = "2")
	private Long categoryId = null;
	@Schema(description = "조회 최소 금액을 입력해주세요(미입력 시 금액 상관 없이 전체 조회)", example = "3000")
	private Integer minPrice = -1;
	@Schema(description = "조회 최대 금액을 입력해주세요(미입력 시 금액 상관 없이 전체 조회)", example = "500000")
	private Integer maxPrice = -1;

	@Schema(description = "조회할 페이지 번호를 입력해주세요(기본값 : 0)", example = "0")
	private Integer pageNumber = 0;

	@Schema(description = "페이지당 조회할 지출 내역을 입력해주세요(기본값 : 10)", example = "10")
	private Integer pageLimit = 10;
}
