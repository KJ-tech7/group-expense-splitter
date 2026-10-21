package com.keshav.expensesplitter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_splits")
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(name = "share_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal shareAmount;

    public ExpenseSplit() {
    }

    public ExpenseSplit(Expense expense, String memberName,
                        BigDecimal shareAmount) {
        this.expense = expense;
        this.memberName = memberName;
        this.shareAmount = shareAmount;
    }

    public Long getId() {
        return id;
    }

    public Expense getExpense() {
        return expense;
    }

    public String getMemberName() {
        return memberName;
    }

    public BigDecimal getShareAmount() {
        return shareAmount;
    }
}