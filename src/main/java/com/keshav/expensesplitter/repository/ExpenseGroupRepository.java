package com.keshav.expensesplitter.repository;

import com.keshav.expensesplitter.model.ExpenseGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseGroupRepository
        extends JpaRepository<ExpenseGroup, Long> {
}