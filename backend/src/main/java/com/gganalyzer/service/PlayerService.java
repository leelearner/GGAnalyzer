package com.gganalyzer.service;

import com.gganalyzer.dto.CreatePlayerRequest;
import com.gganalyzer.model.Player;
import com.gganalyzer.model.Team;
import com.gganalyzer.repository.PlayerRepository;
import com.gganalyzer.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    public Player createPlayer(CreatePlayerRequest request) {
        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
        }

        Player player = Player.builder()
                .handle(request.getHandle())
                .role(request.getRole())
                .team(team)
                .photoUrl(request.getPhotoUrl())
                .build();

        return playerRepository.save(player);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
}
