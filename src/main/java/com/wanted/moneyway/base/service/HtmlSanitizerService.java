package com.wanted.moneyway.base.service;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

@Service
public class HtmlSanitizerService {

	// 1) 허용할 태그, 속성, URL 스킴을 화이트리스트로 정의
	private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
		// 블록 요소
		.allowElements("p", "div", "ul", "ol", "li", "h1", "h2", "h3", "blockquote")
		// 인라인 요소
		.allowElements("span", "strong", "em", "br", "code")
		// 링크와 이미지
		.allowElements("a", "img")
		.allowAttributes("href").onElements("a")
		.allowAttributes("src", "alt", "width", "height").onElements("img")
		// href/src 스킴 제한
		.allowUrlProtocols("http", "https")
		// 스타일 속성(필요 시)
		.allowAttributes("style").onElements("span", "p", "div")
		.toFactory();

	/**
	 * @param rawHtml 사용자로부터 넘어온 원시 HTML
	 * @return 안전하게 필터링된 HTML
	 */
	public String sanitize(String rawHtml) {
		return POLICY.sanitize(rawHtml);
	}
}