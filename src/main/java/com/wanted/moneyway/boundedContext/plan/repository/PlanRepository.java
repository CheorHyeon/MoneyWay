package com.wanted.moneyway.boundedContext.plan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.plan.entity.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long>, CustomPlanRespository {
	List<Plan> findAllByMember(Member member);
}
