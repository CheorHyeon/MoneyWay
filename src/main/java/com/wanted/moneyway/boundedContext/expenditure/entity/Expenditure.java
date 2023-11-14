package com.wanted.moneyway.boundedContext.expenditure.entity;

import static jakarta.persistence.GenerationType.*;
import static java.lang.Boolean.*;

import java.time.LocalDate;

import com.querydsl.core.annotations.QueryEntity;
import com.wanted.moneyway.boundedContext.category.entity.Category;
import com.wanted.moneyway.boundedContext.member.entity.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@QueryEntity
public class Expenditure {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	private Integer spendingPrice;

	private String memo;

	private LocalDate spendDate;

	@Builder.Default
	private Boolean isTotal = TRUE;

	@ManyToOne
	private Member member;

	@ManyToOne
	private Category category;
}
