package com.wanted.moneyway.base.security.config;

import static org.springframework.security.config.http.SessionCreationPolicy.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.wanted.moneyway.base.security.filter.AuthenticationExceptionHandlerFilter;
import com.wanted.moneyway.base.security.filter.JwtAuthorizationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthorizationFilter jwtAuthorizationFilter;

	private final AuthenticationExceptionHandlerFilter authenticationExceptionHandlerFilter;
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(
				authorizeHttpRequests -> authorizeHttpRequests
					.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // 아래 모든 설정은 swagger 관련 링크에만 적용
					.requestMatchers(HttpMethod.POST, "/api/*/member/signup").permitAll() // 회원가입 누구나 가능
					.requestMatchers(HttpMethod.POST, "/api/*/member/login").permitAll() // 로그인은 누구나 가능
					// 예외 처리 로직 없을 때 Tomcat이 포워딩 하는 경로 접근 허용하여 예외 메세지 잘 전달되도록 허용
					.requestMatchers("/error").permitAll()
					.anyRequest().authenticated() // 나머지는 인증된 사용자만 가능
			)
			.cors().disable() // 타 도메인에서 API 호출 가능
			.csrf().disable() // CSRF 토큰 끄기
			.httpBasic().disable() // httpBaic 로그인 방식 끄기
			.formLogin().disable() // 폼 로그인 방식 끄기
			.sessionManagement(sessionManagement ->
				sessionManagement.sessionCreationPolicy(STATELESS)
			) // 세션끄기
			.addFilterBefore(
				jwtAuthorizationFilter, // 엑세스 토큰으로 부터 로그인 처리
				UsernamePasswordAuthenticationFilter.class
			)
			.addFilterBefore(
				authenticationExceptionHandlerFilter,
				JwtAuthorizationFilter.class
			);
		return http.build();
	}

}
