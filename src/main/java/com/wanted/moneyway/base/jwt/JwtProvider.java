package com.wanted.moneyway.base.jwt;

import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.wanted.moneyway.standard.util.Ut;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {
	private SecretKey cachedSecretKey;

	@Value("${custom.jwt.secretKey}")
	private String secretKeyPlain;

	private SecretKey _getSecretKey() {
		String keyBase64Encoded = Base64.getEncoder().encodeToString(secretKeyPlain.getBytes());
		return Keys.hmacShaKeyFor(keyBase64Encoded.getBytes());
	}

	public SecretKey getSecretKey() {
		if (cachedSecretKey == null)
			cachedSecretKey = _getSecretKey();

		return cachedSecretKey;
	}

	public String genToken(Map<String, Object> claims, int seconds) {
		long now = new Date().getTime();
		Date accessTokenExpiresIn = new Date(now + 1000L * seconds);

		return Jwts.builder()
			.claim("body", Ut.json.toStr(claims))
			.setExpiration(accessTokenExpiresIn)
			.signWith(getSecretKey(), SignatureAlgorithm.HS512)
			.compact();
	}

	public boolean verify(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(getSecretKey())
				.build()
				.parseClaimsJws(token);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public Map<String, Object> getClaims(String token) {
		String body = Jwts.parserBuilder()
			.setSigningKey(getSecretKey())
			.build()
			.parseClaimsJws(token)
			.getBody()
			.get("body", String.class);

		return Ut.json.toMap(body);
	}
}