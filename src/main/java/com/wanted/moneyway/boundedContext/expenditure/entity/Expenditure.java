package com.wanted.moneyway.boundedContext.expenditure.entity;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDateTime;

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
public class Expenditure {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	private Integer spendingPrice;

	private String memo;

	private LocalDateTime spendDate;

	private Boolean isTotal;

	@ManyToOne
	private Member member;

	@ManyToOne
	private Category category;
}
