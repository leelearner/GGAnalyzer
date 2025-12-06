package com.gganalyzer.service;

import com.gganalyzer.dto.*;
import com.gganalyzer.model.*;
import com.gganalyzer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private PlayerStatsRepository playerStatsRepository;
    @Autowired
    private StageRepository stageRepository;

    public List<StandingsDTO> getStandings(String leagueName) {
        List<Match> matches = matchRepository.findByLeague_Name(leagueName);
        Map<Long, TeamStanding> standingsMap = new HashMap<>();

        for (Match match : matches) {
            if (match.getWinner() == null)
                continue;

            standingsMap.putIfAbsent(match.getTeamA().getId(), new TeamStanding(match.getTeamA()));
            standingsMap.putIfAbsent(match.getTeamB().getId(), new TeamStanding(match.getTeamB()));

            TeamStanding teamA = standingsMap.get(match.getTeamA().getId());
            TeamStanding teamB = standingsMap.get(match.getTeamB().getId());

            if (match.getWinner().getId().equals(match.getTeamA().getId())) {
                teamA.wins++;
                teamB.losses++;
            } else {
                teamB.wins++;
                teamA.losses++;
            }

            teamA.pointDiff += (match.getTeamAScore() - match.getTeamBScore());
            teamB.pointDiff += (match.getTeamBScore() - match.getTeamAScore());
        }

        return standingsMap.values().stream()
                .sorted((a, b) -> {
                    if (b.wins != a.wins)
                        return b.wins - a.wins;
                    return b.pointDiff - a.pointDiff;
                })
                .map(s -> {
                    int totalGames = s.wins + s.losses;
                    String winRate = totalGames == 0 ? "0" : String.valueOf((int) ((double) s.wins / totalGames * 100));
                    return StandingsDTO.builder()
                            .rank(0) // Will set rank after collecting
                            .teamName(s.team.getName())
                            .teamAcronym(s.team.getAcronym())
                            .wins(s.wins)
                            .losses(s.losses)
                            .pointDiff(s.pointDiff)
                            .winRate(winRate)
                            .streak("0W") // Placeholder
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static class TeamStanding {
        Team team;
        int wins;
        int losses;
        int pointDiff;

        TeamStanding(Team team) {
            this.team = team;
        }
    }

    public List<PlayerStatsDTO> getPlayerStats(String stage_name) {
        Stage stage = stageRepository.findByName(stage_name)
                .orElseThrow(() -> new RuntimeException("Stage not found"));
        List<PlayerStats> statsList = playerStatsRepository.findByStage(stage);
        return statsList.stream()
                .map(stat -> PlayerStatsDTO.builder()
                        .playerName(stat.getPlayer().getHandle())
                        .teamAcronym(
                                stat.getPlayer().getTeam() != null ? stat.getPlayer().getTeam().getAcronym() : "FA")
                        .teamLogoUrl(
                                stat.getPlayer().getTeam() != null ? stat.getPlayer().getTeam().getLogoUrl() : null)
                        .role(stat.getPlayer().getRole())
                        .photoUrl(stat.getPlayer().getPhotoUrl())
                        .gamesPlayed(stat.getGamesPlayed())
                        .winRate(stat.getWinRate())
                        .counterPickRate(stat.getCounterPickRate())
                        .kills(stat.getKills())
                        .deaths(stat.getDeaths())
                        .assists(stat.getAssists())
                        .kda(stat.getKda())
                        .killParticipation(stat.getKillParticipation())
                        .killShare(stat.getKillShare())
                        .deathShare(stat.getDeathShare())
                        .firstBloodRate(stat.getFirstBloodRate())
                        .goldDiff10(stat.getGoldDiff10())
                        .xpDiff10(stat.getXpDiff10())
                        .csDiff10(stat.getCsDiff10())
                        .cspm(stat.getCspm())
                        .csSharePost15(stat.getCsSharePost15())
                        .dpm(stat.getDpm())
                        .damageShare(stat.getDamageShare())
                        .damageSharePost15(stat.getDamageSharePost15())
                        .totalDamagePerGame(stat.getTotalDamagePerGame())
                        .earnedGoldPerMinute(stat.getEarnedGoldPerMinute())
                        .goldShare(stat.getGoldShare())
                        .steals(stat.getSteals())
                        .wardsPerMinute(stat.getWardsPerMinute())
                        .controlWardsPerMinute(stat.getControlWardsPerMinute())
                        .wardsClearedPerMinute(stat.getWardsClearedPerMinute())
                        .build())
                .collect(Collectors.toList());
    }

    public List<TeamStatsDTO> getTeamStats() {
        // Currently not supported with CSV import only
        return new ArrayList<>();
    }

    public List<ChampionStatsDTO> getChampionStats() {
        // Currently not supported with CSV import only
        return new ArrayList<>();
    }

    public List<StageDTO> getStages() {
        List<Stage> stages = stageRepository.findAll();
        return stages.stream().map(s -> StageDTO.builder()
                .name(s.getName())
                .build())
                .collect(Collectors.toList());
    }
}
