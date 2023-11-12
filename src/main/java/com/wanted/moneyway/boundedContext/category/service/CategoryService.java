package com.wanted.moneyway.boundedContext.category.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.category.entity.Category;
import com.wanted.moneyway.boundedContext.category.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
	private final CategoryRepository categoryRepository;
	public RsData<List<Category>> getAll() {
		List<Category> allCategories = categoryRepository.findAll();

		if(allCategories.isEmpty()){
			return RsData.of("F-1", "등록된 카테고리가 없습니다.");
		}

		return RsData.of("S-1", "카테고리 목록 조회 성공", allCategories);
	}
}
