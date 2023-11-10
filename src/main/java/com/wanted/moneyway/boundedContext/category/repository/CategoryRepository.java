package com.wanted.moneyway.boundedContext.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wanted.moneyway.boundedContext.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
