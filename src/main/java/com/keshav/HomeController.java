package com.keshav.expensesplitter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "application", "Group Expense Splitter",
            "status", "Running successfully",
            "message", "Welcome! Your expense splitter backend is ready."
        );
    }
}