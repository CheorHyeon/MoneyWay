package com.wanted.moneyway.boundedContext.member.entity;

import static jakarta.persistence.GenerationType.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Member {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	@Column(unique = true)
	private String userName;
	@JsonIgnore
	private String password;
	private String accessToken;

	public List<? extends GrantedAuthority> getGrantedAuthorities() {
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

		grantedAuthorities.add(new SimpleGrantedAuthority("member"));

		if ("admin".equals(userName)) {
			grantedAuthorities.add(new SimpleGrantedAuthority("admin"));
		}

		return grantedAuthorities;
	}
}
