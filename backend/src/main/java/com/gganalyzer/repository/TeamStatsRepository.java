package com.gganalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gganalyzer.model.Stage;
import com.gganalyzer.model.Team;
import com.gganalyzer.model.TeamStats;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamStatsRepository extends JpaRepository<TeamStats, Long> {
    Optional<TeamStats> findByTeamAndStage(Team team, Stage stage);

    List<TeamStats> findByStage(Stage stage);
}
