package com.gganalyzer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Table(name = "team_stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "team_id", "stage_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer gamesPlayed;
    private Integer wins;
    private Integer losses;
    private Double averageGameTime;
    private Integer totalKills;
    private Integer totalDeaths;
    private Double kd;
    private Double combinedKillsPerMinute;
    private Double goldPercentRating;
    private Double goldSpentPerDiff;
    private Double gd15;
    private Double firstBloodPercent;
    private Double firstTowerPercent;
    private Double firstThreeTowersPercent;
    private Double ppg;
    private Double heraldPercent;
    private Double grubPercent;
    private Double firstDragonPercent;
    private Double dragonPercent;
    private Double elderDragonPercent;
    private Double firstBaronPercent;
    private Double baronPercent;
    private Double lanePercent;
    private Double junglePercent;
    private Double wardsPerMinute;
    private Double controlWardsPerMinute;
    private Double wardsClearedPerMinute;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "stage_id")
    private Stage stage;

    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;
}