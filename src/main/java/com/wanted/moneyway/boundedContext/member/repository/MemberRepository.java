package com.wanted.moneyway.boundedContext.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wanted.moneyway.boundedContext.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
