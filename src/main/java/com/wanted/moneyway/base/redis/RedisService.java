package com.wanted.moneyway.base.redis;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

	private final MemberService memberService;

	@Cacheable(value = "Refresh", key = "#targetId")
	public String getRefreshTokenByCached(long targetId) {
		Member member = memberService.get(targetId);
		return member.getRefreshToken();
	}
}
