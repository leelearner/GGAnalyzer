package com.gganalyzer.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gganalyzer.service.StageService;
import com.gganalyzer.dto.StageDTO;

@RestController
@RequestMapping("/api/stages")
@CrossOrigin(origins = "http://localhost:5173")
public class StageController {
    @Autowired
    private StageService stageService;

    @GetMapping
    public List<StageDTO> getAllStages() {
        return stageService.getAllStages();
    }
}
