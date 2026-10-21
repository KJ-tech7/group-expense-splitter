
package com.keshav.expensesplitter;

import com.keshav.expensesplitter.controller.ExpenseController;
import com.keshav.expensesplitter.model.Expense;
import com.keshav.expensesplitter.model.ExpenseGroup;
import com.keshav.expensesplitter.model.ExpenseSplit;
import com.keshav.expensesplitter.repository.ExpenseGroupRepository;
import com.keshav.expensesplitter.repository.ExpenseRepository;
import com.keshav.expensesplitter.repository.ExpenseSplitRepository;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseControllerTest {

    private final ExpenseRepository expenseRepository =
            mock(ExpenseRepository.class);

    private final ExpenseGroupRepository groupRepository =
            mock(ExpenseGroupRepository.class);

    private final ExpenseSplitRepository splitRepository =
            mock(ExpenseSplitRepository.class);

    private final ExpenseController controller = new ExpenseController(
            expenseRepository, groupRepository, splitRepository);

    @Test
    void balancesShouldBeZeroWhenThereAreNoExpenses() {
        ExpenseGroup group = new ExpenseGroup();
        ReflectionTestUtils.setField(group, "id", 1L);
        group.setMembers(List.of("A", "B", "C"));

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));
        when(expenseRepository.findByExpenseGroupId(1L))
                .thenReturn(List.of());

        Map<String, BigDecimal> balances = controller.getBalances(1L);

        assertEquals(new BigDecimal("0.00"), balances.get("A"));
        assertEquals(new BigDecimal("0.00"), balances.get("B"));
        assertEquals(new BigDecimal("0.00"), balances.get("C"));
    }

    @Test
    void balancesShouldIncludeAmountPaidAndSubtractRecordedShares() {
        ExpenseGroup group = new ExpenseGroup();
        ReflectionTestUtils.setField(group, "id", 1L);
        group.setMembers(List.of("A", "B", "C"));

        Expense expense = new Expense();
        ReflectionTestUtils.setField(expense, "id", 10L);
        expense.setAmount(new BigDecimal("900.00"));
        expense.setPaidBy("A");
        expense.setExpenseGroup(group);

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));
        when(expenseRepository.findByExpenseGroupId(1L))
                .thenReturn(List.of(expense));

        when(splitRepository.findByExpenseId(10L))
                .thenReturn(List.of(
                        new ExpenseSplit(
                                expense, "A", new BigDecimal("300.00")),
                        new ExpenseSplit(
                                expense, "B", new BigDecimal("300.00")),
                        new ExpenseSplit(
                                expense, "C", new BigDecimal("300.00"))
                ));

        Map<String, BigDecimal> balances = controller.getBalances(1L);

        assertEquals(new BigDecimal("600.00"), balances.get("A"));
        assertEquals(new BigDecimal("-300.00"), balances.get("B"));
        assertEquals(new BigDecimal("-300.00"), balances.get("C"));
    }

    @Test
    void shouldReturnNotFoundForMissingGroup() {
        when(groupRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getBalances(99L));

        assertEquals(404, exception.getStatusCode().value());
    }
}
