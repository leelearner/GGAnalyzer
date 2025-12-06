package com.gganalyzer.service;

import java.util.List;
import com.gganalyzer.model.Hello;
import com.gganalyzer.repository.*;
import org.springframework.stereotype.Service;

@Service
public class HelloService {

    private final HelloRepository helloRepository;

    public HelloService(HelloRepository helloRepository) {
        this.helloRepository = helloRepository;
    }

    public List<Hello> hello(String content) {
        return this.helloRepository.findByContent(content);
    }

    public List<Hello> hello() {
        return this.helloRepository.findAll();
    }
}
