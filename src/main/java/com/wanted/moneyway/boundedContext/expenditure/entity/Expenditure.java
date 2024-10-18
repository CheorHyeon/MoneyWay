package com.wanted.moneyway.boundedContext.expenditure.entity;

import static jakarta.persistence.GenerationType.*;
import static java.lang.Boolean.*;

import java.time.LocalDate;

import org.hibernate.annotations.DynamicUpdate;

import com.querydsl.core.annotations.QueryEntity;
import com.wanted.moneyway.boundedContext.category.entity.Category;
import com.wanted.moneyway.boundedContext.member.entity.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@DynamicUpdate  // 변경된 속성만 update 쿼리 발생
public class Expenditure {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	private Integer spendingPrice;

	private String memo;

	private LocalDate spendDate;

	@Builder.Default
	private Boolean isTotal = TRUE;

	@ManyToOne(fetch = FetchType.LAZY)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	private Category category;
}
