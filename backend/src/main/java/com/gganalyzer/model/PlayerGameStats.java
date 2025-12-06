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

    private int kills;
    private int deaths;
    private int assists;
    private int goldEarned;
    private int totalDamageDealtToChampions;
    private int totalMinionsKilled;
}
