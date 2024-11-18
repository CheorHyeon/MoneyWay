package com.wanted.moneyway.base.exceptionHandler;

import static org.springframework.http.HttpStatus.*;

import java.util.stream.Collectors;

import javax.security.sasl.AuthenticationException;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wanted.moneyway.base.rsData.RsData;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ApiGlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(NOT_FOUND)
	public RsData<String> errorHandler(MethodArgumentNotValidException exception) {
		String msg = exception
			.getBindingResult()
			.getAllErrors()
			.stream()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.collect(Collectors.joining("/"));

		String data = exception
			.getBindingResult()
			.getAllErrors()
			.stream()
			.map(DefaultMessageSourceResolvable::getCode)
			.collect(Collectors.joining("/"));
		log.info("MethodArgumentNotValidException 발생 msg :: " + msg);

		return RsData.of("F-MethodArgumentNotValidException", msg, data);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	public RsData<String> errorHandler(RuntimeException exception) {
		String msg = exception.getClass().toString();

		String data = exception.getMessage();
		log.info("RuntimeException 발생 msg :: " + msg);

		return RsData.of("F-RuntimeException", msg, data);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(UNAUTHORIZED) // 예외에 따른 HTTP 상태 코드 설정
	public RsData<String> handleAuthenticationException(AuthenticationException exception) {
		String msg = exception.getMessage(); // 예외 메시지 추출
		log.info("AuthenticationException 발생 msg :: " + msg);
		// RsData.of 메서드 호출
		return RsData.of("F-AuthenticationException", msg);
	}
}