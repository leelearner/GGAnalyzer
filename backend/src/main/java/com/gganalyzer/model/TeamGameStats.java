package com.gganalyzer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_game_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamGameStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private String side;
    private Boolean win;

    private Integer kills;
    private Integer deaths;
    private Integer assists;

    private Integer totalGold;
    private Integer earnedGold;

    private Integer towers;
    private Integer inhibitors;
    private Integer barons;
    private Integer dragons;
    private Integer elders;
    private Integer heralds;
    private Integer voidGrubs;

    private Boolean firstBlood;
    private Boolean firstTower;
    private Boolean firstDragon;
    private Boolean firstBaron;
    private Boolean firstHerald;
    private Boolean firstToThreeTowers;

    private String ban1;
    private String ban2;
    private String ban3;
    private String ban4;
    private String ban5;

    private String pick1;
    private String pick2;
    private String pick3;
    private String pick4;
    private String pick5;
}
