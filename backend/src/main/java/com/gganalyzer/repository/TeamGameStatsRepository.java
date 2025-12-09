package com.gganalyzer.repository;

import com.gganalyzer.model.Game;
import com.gganalyzer.model.Team;
import com.gganalyzer.model.TeamGameStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamGameStatsRepository extends JpaRepository<TeamGameStats, Long> {
    List<TeamGameStats> findByGame(Game game);

    List<TeamGameStats> findByTeam(Team team);

    boolean existsByGameAndTeam(Game game, Team team);
}
