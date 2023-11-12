package com.wanted.moneyway.base.security.filter;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

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
		FilterChain filterChain) throws ServletException, IOException, AuthenticationException {

		String requestURI = request.getRequestURI();

		// 회원가입 및 로그인 요청에 대해서는 필터를 건너뛰기
		if (Pattern.matches("/api/.*/member/signup", requestURI) || Pattern.matches("/api/.*/member/login", requestURI)) {
			filterChain.doFilter(request, response);
			return;
		}

		// swagger 문서 접속에 대해서도 필터 건너뛰기
		if (Pattern.matches("/v3/api-docs/.*", requestURI) || Pattern.matches("/swagger-ui/.*", requestURI) || requestURI.startsWith("/swagger-ui.html")) {
			filterChain.doFilter(request, response);
			return;
		}

		String bearerToken = request.getHeader("Authorization");
		String refreshToken = request.getHeader("RefreshToken");

		if (bearerToken == null && refreshToken == null) {
			throw new AuthenticationException("AT와 RT 둘 중 하나는 헤더에 포함시켜주세요");
		}
		// AT가 유효하다면 인증처리
		if (bearerToken != null && isTokenValid(bearerToken.substring("Bearer ".length()))) {
			forceAuthentication(getMemberFromToken(bearerToken.substring("Bearer ".length())));
			filterChain.doFilter(request, response);
			return;  // 다음 필터로 전달되어도 이 메서드는 종료되지 않고 계속 실행하므로 명시적 종료
		}
		// AT유효하지 않은데, RT가 없다면
		if (refreshToken == null) {
			throw new AuthenticationException("만료된 AT입니다. RT를 포함시켜주세요.");
		}
		// RT 유효한지 검사
		if (isTokenValid(refreshToken)) {
			Member member = getMemberFromToken(refreshToken);
			// 사용자 DB에 저장된 RT와 같다면
			if (member.getRefreshToken().equals(refreshToken)) {
				Map<String, Object> newMemberClaims = member.toClaims();
				String newAccessToken = jwtProvider.genToken(newMemberClaims, 60 * 60 * 24 * 1);
				response.addHeader("Authorization", "Bearer " + newAccessToken);
				forceAuthentication(member);
			}
			// 다르다면 변경된 RT이므로 재 로그인
			else {
				throw new AuthenticationException("유효하지 않은 RT입니다. 재 로그인 해주세요.");
			}
		} else {
			throw new AuthenticationException("토큰이 만료되었습니다. 재 로그인 해주세요.");
		}

		filterChain.doFilter(request, response);
	}

	private Member getMemberFromToken(String token) throws AuthenticationException {
		Map<String, Object> claims = jwtProvider.getClaims(token);
		long id = (long)claims.get("id");
		Member member = memberService.get(id);
		if(member == null)
			throw new AuthenticationException("존재하지 않는 사용자입니다.");
		return member;
	}

	private boolean isTokenValid(String token) {
		return jwtProvider.verify(token);
	}

	private void forceAuthentication(Member member) {
		User user = new User(member.getUserName(), member.getPassword(), member.getGrantedAuthorities());
		UsernamePasswordAuthenticationToken authentication =
			UsernamePasswordAuthenticationToken.authenticated(
				user,
				null,
				member.getGrantedAuthorities()
			);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}
}