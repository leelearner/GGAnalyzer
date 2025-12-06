package com.gganalyzer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StandingsDTO {
    private int rank;
    private String teamName;
    private String teamAcronym;
    private int wins;
    private int losses;
    private int pointDiff;
    private String winRate;
    private String streak;
}
