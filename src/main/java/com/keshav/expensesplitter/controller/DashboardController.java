
package com.keshav.expensesplitter.controller;

import com.keshav.expensesplitter.model.ExpenseGroup;
import com.keshav.expensesplitter.repository.ExpenseGroupRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ExpenseGroupRepository groupRepository;

    public DashboardController(ExpenseGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("groups", groupRepository.findAll());
        return "dashboard";
    }

    @GetMapping("/dashboard/test")
    public String dashboardTest(Model model) {
        model.addAttribute("groups", groupRepository.findAll());
        return "dashboard";
    }
}
