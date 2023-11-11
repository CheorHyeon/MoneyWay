package com.wanted.moneyway.boundedContext.member.DTO;

import lombok.Data;

@Data
public class TokenDTO {
	private String accessToken;
	private String refreshToken;

	public TokenDTO (String accessToken, String refreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}
}
