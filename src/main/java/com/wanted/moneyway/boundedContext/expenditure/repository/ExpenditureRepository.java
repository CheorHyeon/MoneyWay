package com.wanted.moneyway.boundedContext.expenditure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wanted.moneyway.boundedContext.expenditure.entity.Expenditure;

public interface ExpenditureRepository extends JpaRepository<Expenditure, Long>, CustomExpenditureRepository {
}