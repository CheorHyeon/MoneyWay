package com.wanted.moneyway.base.redis;

import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.wanted.moneyway.boundedContext.category.entity.Category;
import com.wanted.moneyway.boundedContext.category.repository.CategoryRepository;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

	private final MemberService memberService;

	private final CategoryRepository categoryRepository;

	@Cacheable(value = "Refresh", key = "#targetId")
	public String getRefreshTokenByCached(long targetId) {
		Member member = memberService.get(targetId);
		return member.getRefreshToken();
	}

	@Cacheable(value = "CategoryList")
	public List<Category> getAllCategoriesByCached() {
		return categoryRepository.findAll();
	}

	@Cacheable(value = "Category", key = "#categoryId")
	public Category getCategoryByCached(Long categoryId) {
		return categoryRepository.findById(categoryId).orElse(null);
	}
}
