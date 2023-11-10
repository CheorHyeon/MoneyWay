package com.wanted.moneyway.boundedContext.member.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	@Transactional
	public RsData join(String account, String password) {
		Optional<Member> opMember = memberRepository.findByUserName(account);
		if (opMember.isPresent()) {
			return RsData.of("F-1", "이미 가입한 Id가 있습니다.");
		}

		Member member = Member.builder()
			.userName(account)
			.password(passwordEncoder.encode(password))
			.build();

		memberRepository.save(member);

		return RsData.of("S-1", "회원가입 완료 로그인 후 이용해주세요!");
	}
}
