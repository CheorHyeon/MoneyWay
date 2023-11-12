package com.wanted.moneyway.base.initData;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wanted.moneyway.boundedContext.category.entity.Category;
import com.wanted.moneyway.boundedContext.category.repository.CategoryRepository;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.repository.MemberRepository;

@Configuration
@Profile({"dev", "test"})
public class NotProd {
	@Bean
	CommandLineRunner initData(MemberRepository memberRepository, PasswordEncoder passwordEncoder, CategoryRepository categoryRepository) {
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

			memberList.add(user1);
			memberList.add(user2);
			memberList.add(user3);
			memberRepository.saveAll(memberList);

			List<Category> categoryList = new ArrayList<>();

			Category category1 = Category.builder()
				.name("식비")
				.build();

			Category category2 = Category.builder()
				.name("카페/간식")
				.build();

			Category category3 = Category.builder()
				.name("생활")
				.build();

			Category category4 = Category.builder()
				.name("주거")
				.build();

			Category category5 = Category.builder()
				.name("통신")
				.build();

			Category category6 = Category.builder()
				.name("패션쇼핑")
				.build();

			Category category7 = Category.builder()
				.name("뷰티/미용")
				.build();

			Category category8 = Category.builder()
				.name("문화/여가")
				.build();

			Category category9 = Category.builder()
				.name("여행/숙박")
				.build();

			Category category10 = Category.builder()
				.name("교통")
				.build();

			Category category11 = Category.builder()
				.name("교육")
				.build();

			categoryList.add(category1);
			categoryList.add(category2);
			categoryList.add(category3);
			categoryList.add(category4);
			categoryList.add(category5);
			categoryList.add(category6);
			categoryList.add(category7);
			categoryList.add(category8);
			categoryList.add(category9);
			categoryList.add(category10);
			categoryList.add(category11);

			categoryRepository.saveAll(categoryList);
		};
	}
}