package com.wanted.moneyway.base.exceptionHandler;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
@Slf4j
public class CustomBasicErrorController extends BasicErrorController {

	public CustomBasicErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
		super(errorAttributes, errorProperties);
	}

	@RequestMapping
	@Override
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		HttpStatus status = getStatus(request);

		if (status == HttpStatus.NO_CONTENT) {
			return new ResponseEntity<>(status);
		}

		Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));

		log.info("/errors 경로를 통한 Exception 발생 :: body = {}", body);
		return new ResponseEntity<>(body, status);
	}
}
