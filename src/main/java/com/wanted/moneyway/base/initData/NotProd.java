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
	CommandLineRunner initData(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
		CategoryRepository categoryRepository) {
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
		};
	}
}