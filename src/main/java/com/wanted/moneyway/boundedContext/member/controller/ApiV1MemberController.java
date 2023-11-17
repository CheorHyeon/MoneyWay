package com.wanted.moneyway.boundedContext.member.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.member.DTO.TokenDTO;
import com.wanted.moneyway.boundedContext.member.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/member")
@Tag(name = "MemberController", description = "회원가입, 로그인처리 컨트롤러")
public class ApiV1MemberController {
	private final MemberService memberService;

	@Data
	public static class SignupRequest {
		@NotBlank(message = "id를 입력해주세요")
		private String username;
		@NotBlank(message = "pw를 입력해주세요")
		@Pattern(regexp = "^(?![0-9]{10,}$)(?!.*(.)\\1{2,}).{10,}$", message = "비밀번호는 10자 이상 입력해야 하며, 숫자로만 이루어 질 수 없으며 3회 이상 연속되는 문자를 사용할 수 없습니다.")
		private String password;
	}

	@PostMapping("/signup")
	@Operation(summary = "회원가입 API")
	public RsData signup(@Valid @RequestBody SignupRequest signupRequest, BindingResult bindingResult) {
		// 요청 객체에서 입력하지 않은 부분이 있다면 메세지를 담아서 RsData 객체 바로 리턴
		if (bindingResult.hasErrors()) {
			List<String> errorMessages = bindingResult.getAllErrors()
				.stream()
				.map(error -> error.getDefaultMessage())
				.collect(Collectors.toList());
			return RsData.of("F-1", errorMessages.get(0));
		}
		// 입력 이상 없다면 계정 가입
		RsData rsData = memberService.join(signupRequest.getUsername(), signupRequest.getPassword());

		return rsData;
	}

	@Data
	public static class SignInRequest {
		@NotBlank(message = "id를 입력해주세요")
		@Schema(description = "사용자 계정", example = "user123")
		private String username;
		@NotBlank(message = "pw를 입력해주세요")
		@Schema(description = "사용자 비밀번호 (10자 이상, 숫자만으로 구성 불가, 3회 이상 연속되는 문자 사용 불가)", example = "password129")
		private String password;
	}


	@PostMapping("/login")
	@Operation(summary = "JWT, RT 발급(로그인) API")
	public RsData<TokenDTO> login(@Valid @RequestBody SignInRequest signInRequest, BindingResult bindingResult) {
		// 요청 객체에서 입력하지 않은 부분이 있다면 메세지를 담아서 RsData 객체 바로 리턴
		if (bindingResult.hasErrors()) {
			List<String> errorMessages = bindingResult.getAllErrors()
				.stream()
				.map(error -> error.getDefaultMessage())
				.collect(Collectors.toList());
			return RsData.of("F-1", errorMessages.get(0));
		}

		RsData<TokenDTO> rsData = memberService.login(signInRequest.getUsername(), signInRequest.getPassword());

		return rsData;
	}
}
