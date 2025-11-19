package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping({"/", ""})
    public String hello(@RequestParam(name = "name", required = true) String name) {
        if (name == null) {
            return "hello, ";
        }
        // Be forgiving if a trailing slash gets included in the query param
        String cleaned = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
        return "hello, " + cleaned;
    }
}
