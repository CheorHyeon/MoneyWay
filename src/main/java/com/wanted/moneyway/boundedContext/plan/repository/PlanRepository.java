package com.wanted.moneyway.boundedContext.plan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wanted.moneyway.boundedContext.plan.entity.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}
