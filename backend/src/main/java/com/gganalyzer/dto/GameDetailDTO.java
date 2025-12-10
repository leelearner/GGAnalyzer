package com.gganalyzer.dto;

import com.gganalyzer.model.Game;
import com.gganalyzer.model.PlayerGameStats;
import com.gganalyzer.model.TeamGameStats;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameDetailDTO {
    private Game game;
    private List<TeamGameStats> teamGameStats;
    private List<PlayerGameStats> playerGameStats;
}
