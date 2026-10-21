package com.keshav.expensesplitter.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expense_groups")
public class ExpenseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id")
    )
    @Column(name = "member_name", nullable = false)
    private List<String> members = new ArrayList<>();

    public ExpenseGroup() {
    }

    public ExpenseGroup(String name, List<String> members) {
        this.name = name;
        this.members = members;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}