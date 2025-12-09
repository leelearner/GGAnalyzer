package com.gganalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gganalyzer.model.Team;
import com.gganalyzer.repository.TeamRepository;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    public Team findByName(String name) {
        return teamRepository.findByName(name).orElse(null);
    }
}
