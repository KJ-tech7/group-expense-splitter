package com.keshav.expensesplitter.controller;

import com.keshav.expensesplitter.model.ExpenseGroup;
import com.keshav.expensesplitter.repository.ExpenseGroupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class ExpenseGroupController {

    private final ExpenseGroupRepository repository;

    public ExpenseGroupController(ExpenseGroupRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseGroup createGroup(@RequestBody ExpenseGroup group) {
        if (group.getName() == null || group.getName().isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Group name is required"
            );
        }

        if (group.getMembers() == null || group.getMembers().isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "At least one member is required"
            );
        }

        return repository.save(group);
    }

    @GetMapping
    public List<ExpenseGroup> getAllGroups() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ExpenseGroup getGroup(@PathVariable Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Group not found"
            ));
    }
}