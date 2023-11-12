package com.wanted.moneyway.base.security.filter;

import java.io.IOException;
import java.util.Map;

import javax.security.sasl.AuthenticationException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.wanted.moneyway.base.jwt.JwtProvider;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.service.MemberService;
import com.wanted.moneyway.standard.util.Ut;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
	private final JwtProvider jwtProvider;
	private final MemberService memberService;

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String bearerToken = request.getHeader("Authorization");
		String refreshToken = request.getHeader("RefreshToken");

		// JWT의 Accss Token이 유효한 경우 사용자 정보 추출해서 바로 로그인 처리
		if (bearerToken != null && isTokenValid(bearerToken.substring("Bearer ".length()))) {
			forceAuthentication(getMemberFromToken(bearerToken.substring("Bearer ".length())));
		}

		// JWT AT가 유효하지 않은 경우 RT를 검사해서 RT가 아직 유효한지 확인
		else if (refreshToken != null && isTokenValid(refreshToken)) {
			Member member = getMemberFromToken(refreshToken);
			// DB에 저장된 RT와 비교하여 같을 경우 강제 로그인 처리
			if(member.getRefreshToken().equals(refreshToken)) {
				Map<String, Object> newMemberClaims = member.toClaims();;
				// 1일짜리 JWT AT 설정
				String newAccessToken = jwtProvider.genToken(newMemberClaims, 60 * 60 * 24 * 1);
				// 새로운 AT를 response 헤더에 추가하여 반환
				response.addHeader("Authorization", "Bearer " + newAccessToken);
				// 로그인 처리
				forceAuthentication(member);
			}
		}
		filterChain.doFilter(request, response);
	}

	private Member getMemberFromToken(String token) {
		Map<String, Object> claims = jwtProvider.getClaims(token);
		long id = (int)claims.get("id");
		return memberService.get(id);
	}

	private boolean isTokenValid(String token) {
		return jwtProvider.verify(token);
	}

	// 강제로 로그인 처리하는 메소드
	private void forceAuthentication(Member member) {
		User user = new User(member.getUserName(), member.getPassword(), member.getGrantedAuthorities());

		// 스프링 시큐리티 객체에 저장할 authentication 객체를 생성
		UsernamePasswordAuthenticationToken authentication =
			UsernamePasswordAuthenticationToken.authenticated(
				user,
				null,
				member.getGrantedAuthorities()
			);

		// 스프링 시큐리티 내에 우리가 만든 authentication 객체를 저장할 context 생성
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		// context에 authentication 객체를 저장
		context.setAuthentication(authentication);
		// 스프링 시큐리티에 context를 등록
		SecurityContextHolder.setContext(context);
	}
}