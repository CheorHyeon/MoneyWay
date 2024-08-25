package com.wanted.moneyway.boundedContext.member.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
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
	private final ApplicationContext applicationContext;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private MemberService memberService;

	private final RedisTemplate redisTemplate;

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

		if (_member == null)
			return RsData.of("F-1", "존재하지 않는 회원입니다.");

		Member member = _member.get();

		RsData rsData = canGenToken(member, password);

		if (rsData.isFail())
			return rsData;

		String accessToken = genAccessToken(member);
		String refreshToken = genRefreshToken(member);

		// 리프레시 토큰 갱신
		member.updateRefreshToken(refreshToken);
		_updateRefreshToken__Cached(member); // Redis 값 갱신

		return RsData.of("S-1", "토큰 발급 성공", new TokenDTO(accessToken, refreshToken));
	}

	private String _updateRefreshToken__Cached(Member member) {
		if (memberService == null) {
			// 의존성 순환 참조 때문에 RedisService를 의존성 주입 불가
			// 따라서 Context에 등록된 memberService 객체 가져와서 실행
			memberService = applicationContext.getBean("memberService", MemberService.class);
		}

		return memberService.updateRefreshToken__Cached(member);
	}

	@CachePut(value = "Refresh", key = "#member.id")
	public String updateRefreshToken__Cached(Member member) {
		return member.getRefreshToken();
	}

	@CacheEvict(value = "Refresh", key = "#memberId", beforeInvocation = false)
	@Transactional
	public RsData deleteRefreshToken(Long memberId) {
		// 1. beforeInvocation 옵션으로 호출 자체로 Redis 캐시 삭제 지만 미리 지워버리면 입구컷 불가
		// @CacheEvict : 캐시에 값이 업성도 메서드 호출하기에 사전 입구컷 필요
		if (!isRefreshTokenExists(memberId)) {
			return RsData.of("S-1", "사용자 Id : " + memberId + "의 Refresh Token이 이미 초기화 되었습니다. 재로그인 해주세요", null);
		}

		Member member = memberRepository.findById(memberId).orElse(null);
		if(member == null) {
			return RsData.of("F-1", "사용자 Id : " + memberId + " 는 존재하지 않는 회원입니다.");
		}
		// 2. DB 초기화
		member.resetRefreshToken();
		return RsData.of("S-1", "사용자 Id : " + member.getUserName() + "의 Refresh Token 초기화 성공 재로그인 해주세요", null);
	}

	// 캐시에서 Refresh Token 존재 여부 확인
	private boolean isRefreshTokenExists(Long memberId) {
		return redisTemplate.hasKey("Refresh::" + memberId);
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
