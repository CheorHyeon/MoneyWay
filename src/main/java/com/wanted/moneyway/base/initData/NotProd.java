package com.wanted.moneyway.base.initData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wanted.moneyway.boundedContext.category.entity.Category;
import com.wanted.moneyway.boundedContext.category.repository.CategoryRepository;
import com.wanted.moneyway.boundedContext.expenditure.entity.Expenditure;
import com.wanted.moneyway.boundedContext.expenditure.repository.ExpenditureRepository;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.repository.MemberRepository;
import com.wanted.moneyway.boundedContext.plan.dto.PlanDTO;
import com.wanted.moneyway.boundedContext.plan.service.PlanService;

@Configuration
@Profile({"dev", "test"})
public class NotProd {
	@Bean
	CommandLineRunner initData(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
		CategoryRepository categoryRepository, PlanService planService, ExpenditureRepository expenditureRepository) {
		return args -> {
			String password = passwordEncoder.encode("1234");
			List<Member> memberList = new ArrayList<>();
			Member user1 = Member.builder()
				.userName("user1")
				.password(password)
				.build();

			Member user2 = Member.builder()
				.userName("user2")
				.password(password)
				.build();

			Member user3 = Member.builder()
				.userName("user3")
				.password(password)
				.build();

			Member user4 = Member.builder()
				.userName("user4")
				.password(password)
				.build();

			Member cheorHyeon = Member.builder()
				.userName("cheorhyeon")
				.password(password)
				.build();

			memberList.add(user1);
			memberList.add(user2);
			memberList.add(user3);
			memberList.add(user4);
			memberList.add(cheorHyeon);
			memberRepository.saveAll(memberList);

			List<Category> categoryList = new ArrayList<>();
			Category category1 = Category.builder()
				.nameH("식비")
				.nameE("food")
				.build();

			Category category2 = Category.builder()
				.nameH("카페/간식")
				.nameE("cafe")
				.build();

			Category category3 = Category.builder()
				.nameH("교육")
				.nameE("education")
				.build();

			Category category4 = Category.builder()
				.nameH("주거")
				.nameE("dwelling")
				.build();

			Category category5 = Category.builder()
				.nameH("통신")
				.nameE("communication")
				.build();

			Category category6 = Category.builder()
				.nameH("쇼핑")
				.nameE("shopping")
				.build();

			Category category7 = Category.builder()
				.nameH("교통")
				.nameE("transfer")
				.build();

			Category category8 = Category.builder()
				.nameH("기타")
				.nameE("others")
				.build();

			categoryList.add(category1);
			categoryList.add(category2);
			categoryList.add(category3);
			categoryList.add(category4);
			categoryList.add(category5);
			categoryList.add(category6);
			categoryList.add(category7);
			categoryList.add(category8);

			categoryRepository.saveAll(categoryList);

			int cafe = 100_000, dwelling = 100_000, food = 100_000, communication = 100_000,
				education = 100_000, shopping = 100_000, transfer = 100_000, others = 100_000;
			PlanDTO planDTO1 = new PlanDTO(food, cafe, education, dwelling, communication, shopping, transfer, others);

			planService.register(planDTO1, user1.getUserName());
			planService.register(planDTO1, user2.getUserName());
			planService.register(planDTO1, user3.getUserName());

			List<Expenditure> expenditureList = new ArrayList<>();

			Expenditure expenditure1 = Expenditure.builder()
				.member(user1)
				.category(category1)
				.memo("식당 내기 짐")
				.spendingPrice(10_000)
				.spendDate(LocalDate.of(2023, 11, 01))
				.build();

			Expenditure expenditure2 = Expenditure.builder()
				.member(user1)
				.category(category1)
				.memo("식당 내기 짐")
				.spendingPrice(10_000)
				.spendDate(LocalDate.of(2023, 11, 02))
				.build();

			Expenditure expenditure3 = Expenditure.builder()
				.member(user1)
				.category(category1)
				.memo("식당 내기 짐")
				.spendingPrice(10_000)
				.spendDate(LocalDate.of(2023, 11, 03))
				.build();

			Expenditure expenditure4 = Expenditure.builder()
				.member(user2)
				.category(category1)
				.memo("식당 내기 짐")
				.spendingPrice(10_000)
				.spendDate(LocalDate.of(2023, 11, 04))
				.build();

			expenditureList.add(expenditure1);
			expenditureList.add(expenditure2);
			expenditureList.add(expenditure3);
			expenditureList.add(expenditure4);

			for (int i = 1; i <= 10; i++) {
				Expenditure expenditureTest1 = Expenditure.builder()
					.member(user1)
					.category(category1)
					.memo("테스트 식비" + i)
					.spendingPrice(10_000)
					.spendDate(LocalDate.now())
					.build();

				Expenditure expenditureTest2 = Expenditure.builder()
					.member(user1)
					.category(category2)
					.memo("테스트 카페/간식" + i)
					.spendingPrice(10_000)
					.spendDate(LocalDate.now())
					.build();

				expenditureList.add(expenditureTest1);
				expenditureList.add(expenditureTest2);
			}

			expenditureRepository.saveAll(expenditureList);

			// cheorHyeon 더미데이터 생성

			cafe = 100_000;
			dwelling = 400_000;
			food = 500_000;
			communication = 50_000;
			education = 0;
			shopping = 100_000;
			transfer = 50_000;
			others = 100_000;
			PlanDTO planDTO2 = new PlanDTO(food, cafe, education, dwelling, communication, shopping, transfer, others);

			planService.register(planDTO2, cheorHyeon.getUserName());

			List<Expenditure> cheorHyeonExpenditureList = new ArrayList<>();

			Expenditure ex1 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category1)
				.memo("이마트 장보기")
				.spendingPrice(100_000)
				.spendDate(LocalDate.now())
				.build();

			Expenditure ex2 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category1)
				.memo("친구랑 포장마차 붕어빵 내기 짐")
				.spendingPrice(10_000)
				.spendDate(LocalDate.now())
				.build();

			Expenditure ex3 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category2)
				.memo("스타벅스 음료 2잔")
				.spendingPrice(10_000)
				.spendDate(LocalDate.now())
				.build();

			Expenditure ex4 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category4)
				.memo("LH이자")
				.spendingPrice(140_000)
				.spendDate(LocalDate.now())
				.build();

			Expenditure ex5 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category5)
				.memo("통신비")
				.spendingPrice(20_000)
				.spendDate(LocalDate.now())
				.build();

			Expenditure ex6 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category7)
				.memo("저번달 교통대금")
				.spendingPrice(50_000)
				.spendDate(LocalDate.now())
				.build();

			Expenditure ex7 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category8)
				.memo("친구 결혼식 축의금")
				.spendingPrice(100_000)
				.spendDate(LocalDate.now())
				.build();

			cheorHyeonExpenditureList.add(ex1);
			cheorHyeonExpenditureList.add(ex2);
			cheorHyeonExpenditureList.add(ex3);
			cheorHyeonExpenditureList.add(ex4);
			cheorHyeonExpenditureList.add(ex5);
			cheorHyeonExpenditureList.add(ex6);
			cheorHyeonExpenditureList.add(ex7);

			expenditureRepository.saveAll(cheorHyeonExpenditureList);

			/*
				지난달 소비 더미 데이터 생성
			 */

			// 비워줘서 객체 재사용
			cheorHyeonExpenditureList.clear();

			Expenditure ex10 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category1)
				.memo("홍길동과 1차 삼겹살집")
				.spendingPrice(60_000)
				.spendDate(LocalDate.now().minusDays(31))
				.build();

			Expenditure ex11 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category1)
				.memo("홍길동과 2차 포차")
				.spendingPrice(40_000)
				.spendDate(LocalDate.now().minusDays(31))
				.build();

			Expenditure ex12 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category2)
				.memo("빽다방")
				.spendingPrice(2_000)
				.spendDate(LocalDate.now().minusDays(32))
				.build();

			Expenditure ex13 = Expenditure.builder()
				.member(cheorHyeon)
				.category(category2)
				.memo("빽다방")
				.spendingPrice(4_000)
				.spendDate(LocalDate.now().minusDays(32))
				.build();

			cheorHyeonExpenditureList.add(ex10);
			cheorHyeonExpenditureList.add(ex11);
			cheorHyeonExpenditureList.add(ex12);
			cheorHyeonExpenditureList.add(ex13);

			expenditureRepository.saveAll(cheorHyeonExpenditureList);


		};
	}
}