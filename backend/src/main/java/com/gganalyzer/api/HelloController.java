package com.gganalyzer.api;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gganalyzer.model.Hello;
import com.gganalyzer.service.HelloService;

@RestController
@RequestMapping("/api/hello")
@CrossOrigin(origins = "http://localhost:5173")
public class HelloController {
    private static final String template = "hello, %s!";
    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping
    public String hello(@RequestParam(defaultValue = "World") String content) {
        String retContent = this.helloService.hello(content).get(0).getContent();
        return String.format(template, retContent);
    }

    @GetMapping("/all")
    public List<Hello> helloList() {
        return this.helloService.hello();
    }
}
