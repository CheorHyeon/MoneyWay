package com.wanted.moneyway.boundedContext.category.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wanted.moneyway.base.rsData.RsData;
import com.wanted.moneyway.boundedContext.category.entity.Category;
import com.wanted.moneyway.boundedContext.category.service.CategoryService;
import com.wanted.moneyway.boundedContext.member.entity.Member;
import com.wanted.moneyway.boundedContext.member.service.MemberService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/categories")
@Tag(name = "CategoryController", description = "카테고리 컨트롤러")
@SecurityRequirements({
	@SecurityRequirement(name = "bearerAuth"),
	@SecurityRequirement(name = "RefreshToken")
})
public class ApiV1CategoryController {

	private final CategoryService categoryService;

	@GetMapping("")
	public RsData list() {

		RsData<List<Category>> rsCategories = categoryService.getAll();

		return rsCategories;
	}
}
