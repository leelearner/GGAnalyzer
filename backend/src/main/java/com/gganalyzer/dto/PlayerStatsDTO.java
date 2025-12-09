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
    private int gamesPlayed;
    private double winRate;
    private double counterPickRate;
    private int kills;
    private int deaths;
    private int assists;
    private double kda;
    private double killParticipation;
    private double killShare;
    private double deathShare;
    private double firstBloodRate;
    private double goldDiff10;
    private double xpDiff10;
    private double csDiff10;
    private double cspm;
    private double csSharePost15;
    private double dpm;
    private double damageShare;
    private double damageSharePost15;
    private double totalDamagePerGame;
    private double earnedGoldPerMinute;
    private double goldShare;
    private double jungleShare;
    private double laneShare;
    private int steals;
    private double wardsPerMinute;
    private double controlWardsPerMinute;
    private double wardsClearedPerMinute;
}
