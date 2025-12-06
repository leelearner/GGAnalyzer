package com.gganalyzer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamStatsDTO {
    private String teamName;
    private int gamesPlayed;
    private double winRate;
    private double kda;
    private double averageGameDuration;
    private double goldPerMin;
}
