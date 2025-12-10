package com.gganalyzer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StandingsDTO {
    private Integer rank;
    private String teamName;
    private String teamAcronym;
    private Integer wins;
    private Integer losses;
    private Integer pointDiff;
    private String winRate;
    private String streak;
}
