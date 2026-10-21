
package com.keshav.expensesplitter.controller;

import com.keshav.expensesplitter.model.Expense;
import com.keshav.expensesplitter.model.ExpenseGroup;
import com.keshav.expensesplitter.model.ExpenseSplit;
import com.keshav.expensesplitter.repository.ExpenseGroupRepository;
import com.keshav.expensesplitter.repository.ExpenseRepository;
import com.keshav.expensesplitter.repository.ExpenseSplitRepository;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseGroupRepository groupRepository;
    private final ExpenseSplitRepository splitRepository;

    public ExpenseController(
            ExpenseRepository expenseRepository,
            ExpenseGroupRepository groupRepository,
            ExpenseSplitRepository splitRepository) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.splitRepository = splitRepository;
    }

    @PostMapping("/groups/{groupId}/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Expense addExpense(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {

        ExpenseGroup group = findGroup(groupId);

        String description = request.get("description") == null
                ? "" : String.valueOf(request.get("description")).trim();

        String paidBy = request.get("paidBy") == null
                ? "" : String.valueOf(request.get("paidBy")).trim();

        if (description.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Description is required");
        }

        List<String> groupMembers = group.getMembers();

        if (groupMembers == null || groupMembers.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "The group has no members");
        }

        if (!groupMembers.contains(paidBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payer must be a member of the group");
        }

        BigDecimal amount;

        try {
            amount = new BigDecimal(String.valueOf(request.get("amount")));
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A valid amount is required");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be positive with at most 2 decimal places");
        }

        List<String> selectedMembers = groupMembers;

        if (request.containsKey("sharedBy")) {
            Object sharedByObject = request.get("sharedBy");

            if (!(sharedByObject instanceof List<?> rawMembers)
                    || rawMembers.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "sharedBy must be a non-empty list of group members");
            }

            Set<String> uniqueMembers = new LinkedHashSet<>();

            for (Object rawMember : rawMembers) {
                String member = String.valueOf(rawMember).trim();

                if (!groupMembers.contains(member)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Every sharedBy member must belong to the group");
                }

                if (!uniqueMembers.add(member)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Duplicate members are not allowed in sharedBy");
                }
            }

            selectedMembers = new ArrayList<>(uniqueMembers);
        }

        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setPaidBy(paidBy);
        expense.setExpenseGroup(group);

        Expense savedExpense = expenseRepository.save(expense);
        createEqualSplits(savedExpense, selectedMembers);

        return savedExpense;
    }

    @GetMapping("/groups/{groupId}/expenses")
    public List<Expense> getExpenses(@PathVariable Long groupId) {
        findGroup(groupId);
        return expenseRepository.findByExpenseGroupId(groupId);
    }

    @PostMapping("/groups/{groupId}/backfill-splits")
    @Transactional
    public Map<String, Object> backfillSplits(@PathVariable Long groupId) {

        ExpenseGroup group = findGroup(groupId);
        List<Expense> expenses =
                expenseRepository.findByExpenseGroupId(groupId);

        int repaired = 0;

        for (Expense expense : expenses) {
            if (splitRepository.findByExpenseId(expense.getId()).isEmpty()) {
                createEqualSplits(expense, group.getMembers());
                repaired++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("groupId", groupId);
        result.put("expensesChecked", expenses.size());
        result.put("expensesRepaired", repaired);
        result.put("message", "Missing equal splits have been created.");

        return result;
    }

    @GetMapping("/groups/{groupId}/balances")
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getBalances(@PathVariable Long groupId) {
        return calculateBalances(findGroup(groupId));
    }

    @GetMapping("/groups/{groupId}/settlements")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSettlements(
            @PathVariable Long groupId) {

        Map<String, BigDecimal> balances =
                calculateBalances(findGroup(groupId));

        List<Map<String, Object>> creditors = new ArrayList<>();
        List<Map<String, Object>> debtors = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
            BigDecimal balance = entry.getValue();

            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(paymentEntry(entry.getKey(), balance));
            } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(paymentEntry(entry.getKey(), balance.abs()));
            }
        }

        List<Map<String, Object>> settlements = new ArrayList<>();

        int creditorIndex = 0;
        int debtorIndex = 0;

        while (creditorIndex < creditors.size()
                && debtorIndex < debtors.size()) {

            String creditor =
                    (String) creditors.get(creditorIndex).get("member");
            String debtor =
                    (String) debtors.get(debtorIndex).get("member");

            BigDecimal credit =
                    (BigDecimal) creditors.get(creditorIndex).get("amount");
            BigDecimal debt =
                    (BigDecimal) debtors.get(debtorIndex).get("amount");

            BigDecimal payment = credit.min(debt)
                    .setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> transaction = new LinkedHashMap<>();
            transaction.put("from", debtor);
            transaction.put("to", creditor);
            transaction.put("amount", payment);

            settlements.add(transaction);

            credit = credit.subtract(payment);
            debt = debt.subtract(payment);

            creditors.get(creditorIndex).put("amount", credit);
            debtors.get(debtorIndex).put("amount", debt);

            if (credit.compareTo(BigDecimal.ZERO) == 0) {
                creditorIndex++;
            }

            if (debt.compareTo(BigDecimal.ZERO) == 0) {
                debtorIndex++;
            }
        }

        return settlements;
    }

    private ExpenseGroup findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Group not found"));
    }

    private void createEqualSplits(Expense expense, List<String> members) {

        if (members == null || members.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "At least one sharing member is required");
        }

        long totalPaise;

        try {
            totalPaise = expense.getAmount()
                    .movePointRight(2).longValueExact();
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid expense amount");
        }

        long baseShare = totalPaise / members.size();
        long remainingPaise = totalPaise % members.size();

        List<ExpenseSplit> splits = new ArrayList<>();

        for (int i = 0; i < members.size(); i++) {
            long memberPaise = baseShare;

            if (i < remainingPaise) {
                memberPaise++;
            }

            BigDecimal share = BigDecimal.valueOf(memberPaise, 2);

            splits.add(new ExpenseSplit(
                    expense, members.get(i), share));
        }

        splitRepository.saveAll(splits);
    }

    private Map<String, BigDecimal> calculateBalances(ExpenseGroup group) {

        Map<String, BigDecimal> balances = new LinkedHashMap<>();

        for (String member : group.getMembers()) {
            balances.put(member, BigDecimal.ZERO.setScale(2));
        }

        List<Expense> expenses =
                expenseRepository.findByExpenseGroupId(group.getId());

        for (Expense expense : expenses) {
            balances.compute(
                    expense.getPaidBy(),
                    (member, balance) -> balance.add(expense.getAmount()));

            List<ExpenseSplit> splits =
                    splitRepository.findByExpenseId(expense.getId());

            for (ExpenseSplit split : splits) {
                balances.computeIfPresent(
                        split.getMemberName(),
                        (member, balance) ->
                                balance.subtract(split.getShareAmount()));
            }
        }

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        balances.forEach((member, balance) ->
                result.put(member, balance.setScale(2, RoundingMode.HALF_UP)));

        return result;
    }

    private Map<String, Object> paymentEntry(
            String member, BigDecimal amount) {

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("member", member);
        entry.put("amount", amount);
        return entry;
    }
}
