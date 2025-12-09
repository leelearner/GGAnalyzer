package com.gganalyzer.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "games")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    @JsonIgnoreProperties("games")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Match match;

    private Integer gameNumber; // 1, 2, 3...

    private Long durationSeconds;

    @ManyToOne
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    private String patchVersion;

    private String gameId;
}
