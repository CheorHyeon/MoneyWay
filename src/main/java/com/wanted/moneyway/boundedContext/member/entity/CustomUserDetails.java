package com.wanted.moneyway.boundedContext.member.entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
	private final Member member;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return member.getGrantedAuthorities();
	}

	@Override
	public String getPassword() {
		return null; // 굳이 내려줄 필요 없음
	}

	@Override
	public String getUsername() {
		return member.getUserName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	// 위와 같이 UserDetails를 커스텀 할 경우 실제 인증한 Member 객체를 추출하는 등 추가 기능 가능
	public Member getMember() {
		return member;
	}
}
