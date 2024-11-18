package com.wanted.moneyway.base.exceptionHandler;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrorConfig {

	@Bean
	@ConfigurationProperties(prefix = "server.error")
	public ErrorProperties errorProperties() {
		return new ErrorProperties();
	}
}