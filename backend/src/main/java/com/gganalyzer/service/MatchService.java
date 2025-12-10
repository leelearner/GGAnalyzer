package com.gganalyzer.service;

import com.gganalyzer.model.Match;
import com.gganalyzer.model.Team;
import com.gganalyzer.repository.MatchRepository;
import com.gganalyzer.repository.PlayerGameStatsRepository;
import com.gganalyzer.repository.TeamGameStatsRepository;
import com.gganalyzer.dto.GameDetailDTO;
import com.gganalyzer.dto.MatchDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamGameStatsRepository teamGameStatsRepository;

    @Autowired
    private PlayerGameStatsRepository playerGameStatsRepository;

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Match findMatchByDateAndTeams(String date, String teamAName, String teamBName) {
        Team teamA = teamService.findByName(teamAName);
        Team teamB = teamService.findByName(teamBName);
        if (teamA == null || teamB == null) {
            throw new IllegalArgumentException("One or both teams not found");
        }
        return matchRepository.findByDateAndTeamAAndTeamB(date, teamA, teamB).orElse(null);
    }

    public void increaseTeamAScore(Match match) {
        match.setTeamAScore(match.getTeamAScore() + 1);
        matchRepository.save(match);
        setMatchWinner(match);
    }

    public void increaseTeamBScore(Match match) {
        match.setTeamBScore(match.getTeamBScore() + 1);
        matchRepository.save(match);
        setMatchWinner(match);
    }

    public void setMatchWinner(Match match) {
        Team winner = match.getTeamAScore() > match.getTeamBScore() ? match.getTeamA() : match.getTeamB();
        match.setWinner(winner);
        matchRepository.save(match);
    }

    public Match findByMathId(String matchId) {
        return matchRepository.findByMatchId(matchId).orElse(null);
    }

    public MatchDetailDTO getMatchDetail(String matchId) {
        Match match = findByMathId(matchId);
        if (match == null)
            return null;

        List<GameDetailDTO> gameDetails = match.getGames().stream().map(game -> {
            return GameDetailDTO.builder()
                    .game(game)
                    .teamGameStats(teamGameStatsRepository.findByGame(game))
                    .playerGameStats(playerGameStatsRepository.findByGame(game))
                    .build();
        }).toList();

        return MatchDetailDTO.builder()
                .match(match)
                .games(gameDetails)
                .build();
    }
}
