package com.wanted.moneyway.boundedContext.expenditure.dto;

import static java.lang.Boolean.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.wanted.moneyway.boundedContext.expenditure.entity.Expenditure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenditureDTO {
	@NotNull(message = "카테고리 Id를 입력해주세요")
	@Schema(description = "카테고리 Id 입력", example = "1")
	private Long categoryId;
	@NotNull(message = "소비 금액을 입력해주세요.")
	@Schema(description = "소비 금액, 천단위 구분기호 없이", example = "10000")
	private Integer spendingPrice;
	@Schema(description = "비고란", example = "식당 내기 짐")
	private String memo;
	@NotNull(message = "지출 일자를 입력해주세요")
	@Schema(description = "지출 일자 형식")
	private LocalDate spendDate;
	@Schema(description = "지출 합계에 포함 시킬지 여부(기본값 true)", example = "true(기본값) / false")
	private Boolean isTotal = TRUE;

	public static ExpenditureDTO of(Expenditure expenditure) {
		return ExpenditureDTO.builder()
			.categoryId(expenditure.getCategory().getId())
			.isTotal(expenditure.getIsTotal())
			.memo(expenditure.getMemo())
			.spendDate(expenditure.getSpendDate())
			.spendingPrice(expenditure.getSpendingPrice())
			.build();
	}
}
