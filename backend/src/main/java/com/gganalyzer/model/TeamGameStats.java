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
    private boolean win;

    private int kills;
    private int deaths;
    private int assists;

    private int totalGold;
    private int earnedGold;

    private int towers;
    private int inhibitors;
    private int barons;
    private int dragons;
    private int elders;
    private int heralds;
    private int voidGrubs;

    private boolean firstBlood;
    private boolean firstTower;
    private boolean firstDragon;
    private boolean firstBaron;
    private boolean firstHerald;
    private boolean firstToThreeTowers;

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
