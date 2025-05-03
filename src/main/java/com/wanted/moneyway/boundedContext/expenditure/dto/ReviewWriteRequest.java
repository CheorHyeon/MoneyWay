package com.wanted.moneyway.boundedContext.expenditure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ReviewWriteRequest(
	@Schema(description = "이번달 회고록 제목", example = "4월 총 사용 회고록")
	@NotBlank(message = "제목은 필수 항목입니다.")
	String title,
	@Schema(description = "이번달 회고록 내용", example = "<p> 병원비.. 눈물.. </p>")
	@NotBlank(message = "본문 내용은 필수 항목입니다.")
	String content
) {
}
