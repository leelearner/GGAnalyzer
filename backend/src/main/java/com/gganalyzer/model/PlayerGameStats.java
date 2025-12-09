package com.gganalyzer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_game_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerGameStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne
    @JoinColumn(name = "champion_id")
    private Champion champion;

    private String side;
    private String position;

    private Integer participantId;
    private String dataCompleteness;
    private String url;

    private Integer result;
    private Double gameLength;

    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Double kda;
    private Integer teamKills;
    private Integer teamDeaths;

    private Integer doubleKills;
    private Integer tripleKills;
    private Integer quadraKills;
    private Integer pentaKills;

    private Boolean firstBlood;
    private Boolean firstBloodKill;
    private Boolean firstBloodAssist;
    private Boolean firstBloodVictim;

    private Double teamKpm;
    private Double ckpm;

    // Objectives
    private Integer firstDragon;
    private Integer dragons;
    private Integer oppDragons;
    private Integer elementalDrakes;
    private Integer oppElementalDrakes;
    private Integer infernals;
    private Integer mountains;
    private Integer clouds;
    private Integer oceans;
    private Integer chemtechs;
    private Integer hextechs;
    private Integer dragonsTypeUnknown;
    private Integer elders;
    private Integer oppElders;
    private Integer firstHerald;
    private Integer heralds;
    private Integer oppHeralds;
    private Integer voidGrubs;
    private Integer oppVoidGrubs;
    private Integer firstBaron;
    private Integer barons;
    private Integer oppBarons;
    private Integer atakhans;
    private Integer oppAtakhans;
    private Integer firstTower;
    private Integer towers;
    private Integer oppTowers;
    private Integer firstMidTower;
    private Integer firstToThreeTowers;
    private Integer turretPlates;
    private Integer oppTurretPlates;
    private Integer inhibitors;
    private Integer oppInhibitors;

    // Damage
    private Double damageToChampions;
    private Double dpm;
    private Double damageShare;
    private Double damageTakenPerMinute;
    private Double damageMitigatedPerMinute;
    private Double damageToTowers;

    // Vision
    private Integer wardsPlaced;
    private Double wpm;
    private Integer wardsKilled;
    private Double wcpm;
    private Integer controlWardsBought;
    private Double visionScore;
    private Double vspm;

    // Gold & CS
    private Integer totalGold;
    private Integer earnedGold;
    private Double earnedGpm;
    private Double earnedGoldShare;
    private Integer goldSpent;
    private Double gspd;
    private Double gpr;
    private Integer totalCs;
    private Integer minionKills;
    private Integer monsterKills;
    private Integer monsterKillsOwnJungle;
    private Integer monsterKillsEnemyJungle;
    private Double cspm;

    // Early Game Stats (10 min)
    private Integer goldAt10;
    private Integer xpAt10;
    private Integer csAt10;
    private Integer oppGoldAt10;
    private Integer oppXpAt10;
    private Integer oppCsAt10;
    private Integer goldDiffAt10;
    private Integer xpDiffAt10;
    private Integer csDiffAt10;
    private Integer killsAt10;
    private Integer assistsAt10;
    private Integer deathsAt10;
    private Integer oppKillsAt10;
    private Integer oppAssistsAt10;
    private Integer oppDeathsAt10;

    // Early Game Stats (15 min)
    private Integer goldAt15;
    private Integer xpAt15;
    private Integer csAt15;
    private Integer oppGoldAt15;
    private Integer oppXpAt15;
    private Integer oppCsAt15;
    private Integer goldDiffAt15;
    private Integer xpDiffAt15;
    private Integer csDiffAt15;
    private Integer killsAt15;
    private Integer assistsAt15;
    private Integer deathsAt15;
    private Integer oppKillsAt15;
    private Integer oppAssistsAt15;
    private Integer oppDeathsAt15;

    // Early Game Stats (20 min)
    private Integer goldAt20;
    private Integer xpAt20;
    private Integer csAt20;
    private Integer oppGoldAt20;
    private Integer oppXpAt20;
    private Integer oppCsAt20;
    private Integer goldDiffAt20;
    private Integer xpDiffAt20;
    private Integer csDiffAt20;
    private Integer killsAt20;
    private Integer assistsAt20;
    private Integer deathsAt20;
    private Integer oppKillsAt20;
    private Integer oppAssistsAt20;
    private Integer oppDeathsAt20;

    // Early Game Stats (25 min)
    private Integer goldAt25;
    private Integer xpAt25;
    private Integer csAt25;
    private Integer oppGoldAt25;
    private Integer oppXpAt25;
    private Integer oppCsAt25;
    private Integer goldDiffAt25;
    private Integer xpDiffAt25;
    private Integer csDiffAt25;
    private Integer killsAt25;
    private Integer assistsAt25;
    private Integer deathsAt25;
    private Integer oppKillsAt25;
    private Integer oppAssistsAt25;
    private Integer oppDeathsAt25;

}
