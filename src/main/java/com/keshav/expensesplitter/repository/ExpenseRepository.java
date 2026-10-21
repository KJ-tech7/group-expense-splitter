package com.keshav.expensesplitter.repository;

import com.keshav.expensesplitter.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByExpenseGroupId(Long groupId);
}