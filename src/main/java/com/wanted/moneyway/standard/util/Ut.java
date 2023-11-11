package com.wanted.moneyway.standard.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Ut {
	public static class json {
		// map을 Json 형태 변환 매서드
		public static Object toStr(Map<String, Object> map) {
			try {
				// Jackson 라이브러리 ObjectMapper 클래스 사용하여 map 객체를 Json형태
				// Java <-> Json 처리 작업 간단하게 해주는 클래스
				return new ObjectMapper().writeValueAsString(map);
			} catch (JsonProcessingException e) {
				return null;
			}
		}

		// json 형태 데이터를 map 형태로 변환
		public static Map<String, Object> toMap(String jsonStr) {
			try {
				return new ObjectMapper().readValue(jsonStr, LinkedHashMap.class);
			} catch (JsonProcessingException e) {
				return null;
			}
		}
	}
}