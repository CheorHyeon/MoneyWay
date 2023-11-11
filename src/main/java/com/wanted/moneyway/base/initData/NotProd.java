package com.wanted.moneyway.base.initData;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.repository.MemberRepository;

@Configuration
@Profile({"dev", "test"})
public class NotProd {
	@Bean
	CommandLineRunner initData(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
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

			memberList.addAll(List.of(user1, user2, user3));
			memberRepository.saveAll(memberList);
		};
	}
}