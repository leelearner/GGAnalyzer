package com.gganalyzer.dto;

import com.gganalyzer.model.Team;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StandingsDTO {
    private Integer rank;
    private String teamName;
    private String teamAcronym;
    private Team team;
    private Integer wins;
    private Integer losses;
    private Integer pointDiff;
    private String winRate;
    private String streak;
}
