package com.wanted.moneyway.base.security.config;

import static org.springframework.security.config.http.SessionCreationPolicy.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/api/**") // 아래 모든 설정은 /api/** 경로에만 적용
			.authorizeHttpRequests(
				authorizeHttpRequests -> authorizeHttpRequests
					.requestMatchers("/api/*/member/signup").permitAll() // 로그인은 누구나 가능
					.requestMatchers("/api/*/member/login").permitAll() // 로그인은 누구나 가능
					.anyRequest().authenticated() // 나머지는 인증된 사용자만 가능
			)
			.cors().disable() // 타 도메인에서 API 호출 가능
			.csrf().disable() // CSRF 토큰 끄기
			.httpBasic().disable() // httpBaic 로그인 방식 끄기
			.formLogin().disable() // 폼 로그인 방식 끄기
			.sessionManagement(sessionManagement ->
				sessionManagement.sessionCreationPolicy(STATELESS)
			); // 세션끄기

		return http.build();
	}
}
