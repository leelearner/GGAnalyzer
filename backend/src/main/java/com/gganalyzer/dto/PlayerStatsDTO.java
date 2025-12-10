package com.gganalyzer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerStatsDTO {
    private String playerName;
    private String teamAcronym;
    private String teamLogoUrl;
    private String role;
    private String photoUrl;
    private Integer gamesPlayed;
    private Double winRate;
    private Double counterPickRate;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Double kda;
    private Double killParticipation;
    private Double killShare;
    private Double deathShare;
    private Double firstBloodRate;
    private Double goldDiff10;
    private Double xpDiff10;
    private Double csDiff10;
    private Double cspm;
    private Double csSharePost15;
    private Double dpm;
    private Double damageShare;
    private Double damageSharePost15;
    private Double totalDamagePerGame;
    private Double earnedGoldPerMinute;
    private Double goldShare;
    private Double jungleShare;
    private Double laneShare;
    private Integer steals;
    private Double wardsPerMinute;
    private Double controlWardsPerMinute;
    private Double wardsClearedPerMinute;
}
