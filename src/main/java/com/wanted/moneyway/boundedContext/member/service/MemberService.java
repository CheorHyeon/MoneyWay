package com.wanted.moneyway.boundedContext.member.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanted.moneyway.base.jwt.JwtProvider;
import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.member.DTO.TokenDTO;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	private final JwtProvider jwtProvider;
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

	@Transactional
	public RsData<TokenDTO> login(String name, String password) {
		Optional<Member> _member = memberRepository.findByUserName(name);

		if(_member == null)
			return	RsData.of("F-1", "존재하지 않는 회원입니다.");

		Member member = _member.get();

		RsData rsData = canGenToken(member, password);

		if (rsData.isFail())
			return rsData;

		String accessToken = genAccessToken(member);
		String refreshToken = genRefreshToken(member);

		// 리프레시 토큰 갱신
		member.updateRefreshToken(refreshToken);

		return RsData.of("S-1", "토큰 발급 성공", new TokenDTO(accessToken, refreshToken));

	}

	private String genRefreshToken(Member member) {
		Map<String, Object> memberInfo = member.toClaims();
		memberInfo.put("Type", "Refresh");
		// 3일짜리 RT 발급
		return jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 3);
	}

	private RsData canGenToken(Member member, String password) {
		if (!passwordEncoder.matches(password, member.getPassword())) {
			return RsData.of("F-1", "비밀번호가 일치하지 않습니다.");
		}

		return RsData.of("S-1", "AT, RT를 생성할 수 있습니다.");
	}

	public String genAccessToken(Member member) {
		// 1일 짜리 JWT 발급
		return jwtProvider.genToken(member.toClaims(), 60 * 60 * 24 * 1);
	}

	public Member get(long memberId) {
		return memberRepository.findById(memberId).get();
	}

	public Member get(String userName) {
		return memberRepository.findByUserName(userName).get();
	}

	public Member createByClaims(Map<String, Object> claims) {
		return Member.builder()
			.id((long)(int)claims.get("id"))
			.userName((String)claims.get("userName"))
			.build();
	}
}
