package com.gganalyzer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChampionStatsDTO {
    private String championName;
    private int gamesPlayed;
    private double winRate;
    private double pickRate;
    private double banRate;
    private double kda;
}
