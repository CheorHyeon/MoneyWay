package com.wanted.moneyway.boundedContext.plan.repository;

import static com.wanted.moneyway.boundedContext.plan.entity.QPlan.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wanted.moneyway.boundedContext.plan.dto.PlanPercentDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CustomPlanRespositoryImpl implements CustomPlanRespository{

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public PlanPercentDTO recommendPercent() {

		// 카테고리별 평균 비율
		List<Tuple> results = jpaQueryFactory
			.select(plan.category.nameE, plan.categoryRatio.avg())
			.from(plan)
			.groupBy(plan.category.id)
			.fetch();
		// DTO 객체 생성
		PlanPercentDTO dto = new PlanPercentDTO();
		for(Tuple t : results) {
			String nameE = t.get(plan.category.nameE);
			Double average = t.get(plan.categoryRatio.avg());

			try {
				// 속성명으로 메서드를 찾아내고, 값을 설정합니다.
				// setter 메서드 호출하여 값 지정
				Method method = dto.getClass().getMethod("set" + Character.toUpperCase(nameE.charAt(0)) + nameE.substring(1), Double.class);
				method.invoke(dto, average);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				// 로그를 남깁니다.
				log.error("비율 계산 DTO 설정 예외 발생", e);
			}
		}
		return dto;
	}
}
