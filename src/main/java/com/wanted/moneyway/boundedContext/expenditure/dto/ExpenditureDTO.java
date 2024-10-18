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

@Builder
public record ExpenditureDTO (
	@Schema(description = "지출내역 id, 응답용 필드", accessMode = Schema.AccessMode.READ_ONLY)
	Long expenditureId,
	@NotNull(message = "카테고리 Id를 입력해주세요")
	@Schema(description = "카테고리 Id 입력", example = "1")
	Long categoryId,
	@NotNull(message = "소비 금액을 입력해주세요.")
	@Schema(description = "소비 금액, 천단위 구분기호 없이", example = "10000")
	Integer spendingPrice,
	@Schema(description = "비고란", example = "식당 내기 짐")
	String memo,
	@NotNull(message = "지출 일자를 입력해주세요")
	@Schema(description = "지출 일자 형식")
	LocalDate spendDate,
	@Schema(description = "지출 합계에 포함 시킬지 여부(기본값 true)", example = "true(기본값) / false")
	Boolean isTotal){
	public static ExpenditureDTO of(Expenditure expenditure) {
		return ExpenditureDTO.builder()
			.expenditureId(expenditure.getId())
			.categoryId(expenditure.getCategory().getId())
			.isTotal(expenditure.getIsTotal())
			.memo(expenditure.getMemo())
			.spendDate(expenditure.getSpendDate())
			.spendingPrice(expenditure.getSpendingPrice())
			.build();
	}

	public boolean checkAllNull() {
		return categoryId == null && spendingPrice == null && memo == null && spendDate == null && isTotal == null;
	}
}
