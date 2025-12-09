package com.gganalyzer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gganalyzer.repository.StageRepository;
import com.gganalyzer.dto.StageDTO;
import com.gganalyzer.model.Stage;

@Service
public class StageService {
    @Autowired
    private StageRepository stageRepository;

    public List<StageDTO> getAllStages() {
        List<Stage> stages = stageRepository.findAll();
        // You might want to convert List<Stage> to List<StageDTO> here
        return stages.stream().map(stage -> StageDTO.builder()
                .name(stage.getName())
                .build()).toList();
    }
}
