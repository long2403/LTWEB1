package com.example.lab1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Bai2 {

    @GetMapping("/greet")
    public String greet(@RequestParam String name) {
        return "Xin chào " + name;
    }
}
