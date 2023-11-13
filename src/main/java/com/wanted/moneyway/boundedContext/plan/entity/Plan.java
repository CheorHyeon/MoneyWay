package com.wanted.moneyway.boundedContext.plan.entity;

import static jakarta.persistence.GenerationType.*;

import org.hibernate.annotations.DynamicUpdate;

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
@DynamicUpdate
public class Plan {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ManyToOne
	private Member member;

	@ManyToOne
	private Category category;

	private Integer budget;

	private double categoryRatio;

	public void update(int budget) {
		this.budget = budget;
	}

	public void updateCategoryRatio(double categoryRatio) {
		this.categoryRatio = categoryRatio;
	}
}
