package com.gganalyzer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "player_id", "stage" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne
    @JoinColumn(name = "stage")
    private Stage stage;

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

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        lastUpdated = LocalDateTime.now();
    }
}
