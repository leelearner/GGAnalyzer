package com.gganalyzer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne
    @JoinColumn(name = "stage")
    private Stage stage;

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
    private int goldDiff10;
    private int xpDiff10;
    private double csDiff10;
    private double cspm;
    private double csSharePost15;
    private double dpm;
    private double damageShare;
    private double damageSharePost15;
    private int totalDamagePerGame;
    private double earnedGoldPerMinute;
    private double goldShare;
    private int steals;
    private double wardsPerMinute;
    private double controlWardsPerMinute;
    private double wardsClearedPerMinute;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        lastUpdated = LocalDateTime.now();
    }
}
