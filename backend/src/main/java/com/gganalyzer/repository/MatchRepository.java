package com.gganalyzer.repository;

import com.gganalyzer.model.Match;
import com.gganalyzer.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByLeague_Name(String leagueName);

    Optional<Match> findByMatchId(String matchId);

    Optional<Match> findByDateAndTeamAAndTeamB(String date, Team teamA, Team teamB);

    List<Match> findByStage(com.gganalyzer.model.Stage stage);
}
