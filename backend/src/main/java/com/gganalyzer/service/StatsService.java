package com.gganalyzer.service;

import com.gganalyzer.dto.*;
import com.gganalyzer.model.*;
import com.gganalyzer.repository.*;

import jakarta.transaction.Transactional;

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

    @Autowired
    private PlayerGameStatsRepository playerGameStatsRepository;

    private Object lock = new Object();

    @Transactional
    public void calculatePlayerStats() {
        List<Stage> stages = stageRepository.findAll();
        for (Stage stage : stages) {
            List<Match> matches = matchRepository.findByStage(stage);
            if (matches.isEmpty())
                continue;

            // Collect all games in this stage
            List<Game> games = new ArrayList<>();
            for (Match match : matches) {
                games.addAll(match.getGames());
            }
            if (games.isEmpty())
                continue;

            // Collect all player game stats in these games
            List<PlayerGameStats> allStats = new ArrayList<>();
            for (Game game : games) {
                allStats.addAll(playerGameStatsRepository.findByGame(game));
            }

            // Pre-calculate game totals for shares
            Map<Long, Map<String, GameSideTotals>> gameSideTotals = new HashMap<>();
            Map<Long, GameTotals> gameTotals = new HashMap<>();

            for (PlayerGameStats pgs : allStats) {
                Long gameId = pgs.getGame().getId();
                String side = pgs.getSide(); // "Blue" or "Red"

                gameSideTotals.putIfAbsent(gameId, new HashMap<>());
                gameSideTotals.get(gameId).putIfAbsent(side, new GameSideTotals());
                gameTotals.putIfAbsent(gameId, new GameTotals());

                GameSideTotals sideTotals = gameSideTotals.get(gameId).get(side);
                GameTotals gTotals = gameTotals.get(gameId);

                // CS Post 15
                if (pgs.getTotalCs() != null && pgs.getCsAt15() != null) {
                    sideTotals.totalCsPost15 += (pgs.getTotalCs() - pgs.getCsAt15());
                }

                // Jungle CS
                if (pgs.getMonsterKills() != null) {
                    gTotals.totalJungleCs += pgs.getMonsterKills();
                }

                // Lane CS
                if (pgs.getMinionKills() != null) {
                    gTotals.totalLaneCs += pgs.getMinionKills();
                }
            }

            // Group by Player
            Map<Player, List<PlayerGameStats>> playerStatsMap = allStats.stream()
                    .collect(Collectors.groupingBy(PlayerGameStats::getPlayer));

            for (Map.Entry<Player, List<PlayerGameStats>> entry : playerStatsMap.entrySet()) {
                Player player = entry.getKey();
                List<PlayerGameStats> pgsList = entry.getValue();

                if (pgsList.isEmpty())
                    continue;

                synchronized (lock) {
                    PlayerStats stats = playerStatsRepository.findByPlayerAndStage(player, stage)
                            .orElse(PlayerStats.builder().player(player).stage(stage).build());

                    updatePlayerStats(stats, pgsList, games, gameSideTotals, gameTotals);
                    playerStatsRepository.save(stats);
                }
            }
        }
    }

    private static class GameSideTotals {
        int totalCsPost15 = 0;
    }

    private static class GameTotals {
        int totalJungleCs = 0;
        int totalLaneCs = 0;
    }

    private void updatePlayerStats(PlayerStats stats, List<PlayerGameStats> pgsList, List<Game> stageGames,
            Map<Long, Map<String, GameSideTotals>> gameSideTotals, Map<Long, GameTotals> gameTotals) {
        int gamesPlayed = pgsList.size();
        stats.setGamesPlayed(gamesPlayed);

        // Basic sums
        int kills = 0;
        int deaths = 0;
        int assists = 0;
        long totalDamage = 0;
        long totalGold = 0;
        int totalCs = 0;
        int totalWardsPlaced = 0;
        int totalWardsKilled = 0;
        int totalControlWards = 0;
        double totalDurationMinutes = 0;

        int wins = 0;
        int firstBloodKills = 0;

        double sumGoldDiff10 = 0;
        int countGoldDiff10 = 0;
        double sumXpDiff10 = 0;
        int countXpDiff10 = 0;
        double sumCsDiff10 = 0;
        int countCsDiff10 = 0;

        double sumDamageShare = 0;
        double sumGoldShare = 0;
        double sumVisionScore = 0;

        double sumKillShare = 0;
        double sumDeathShare = 0;
        double sumKillParticipation = 0;
        double sumCsSharePost15 = 0;
        double sumJungleShare = 0;
        double sumLaneShare = 0;

        for (PlayerGameStats pgs : pgsList) {
            kills += pgs.getKills() != null ? pgs.getKills() : 0;
            deaths += pgs.getDeaths() != null ? pgs.getDeaths() : 0;
            assists += pgs.getAssists() != null ? pgs.getAssists() : 0;
            totalDamage += pgs.getDamageToChampions() != null ? pgs.getDamageToChampions().longValue() : 0;
            totalGold += pgs.getTotalGold() != null ? pgs.getTotalGold() : 0;
            totalCs += pgs.getTotalCs() != null ? pgs.getTotalCs() : 0;
            totalWardsPlaced += pgs.getWardsPlaced() != null ? pgs.getWardsPlaced() : 0;
            totalWardsKilled += pgs.getWardsKilled() != null ? pgs.getWardsKilled() : 0;
            totalControlWards += pgs.getControlWardsBought() != null ? pgs.getControlWardsBought() : 0;

            if (pgs.getFirstBloodKill() != null && pgs.getFirstBloodKill())
                firstBloodKills++;

            if (pgs.getGoldDiffAt10() != null) {
                sumGoldDiff10 += pgs.getGoldDiffAt10();
                countGoldDiff10++;
            }
            if (pgs.getXpDiffAt10() != null) {
                sumXpDiff10 += pgs.getXpDiffAt10();
                countXpDiff10++;
            }
            if (pgs.getCsDiffAt10() != null) {
                sumCsDiff10 += pgs.getCsDiffAt10();
                countCsDiff10++;
            }

            sumDamageShare += pgs.getDamageShare() != null ? pgs.getDamageShare() : 0;
            sumGoldShare += pgs.getEarnedGoldShare() != null ? pgs.getEarnedGoldShare() : 0;
            sumVisionScore += pgs.getVisionScore() != null ? pgs.getVisionScore() : 0;

            // Calculate Shares
            if (pgs.getKills() != null && pgs.getTeamKills() != null && pgs.getTeamKills() > 0) {
                sumKillShare += (double) pgs.getKills() / pgs.getTeamKills();
            }
            if (pgs.getDeaths() != null && pgs.getTeamDeaths() != null && pgs.getTeamDeaths() > 0) {
                sumDeathShare += (double) pgs.getDeaths() / pgs.getTeamDeaths();
            }

            // Calculate Kill Participation
            if (pgs.getTeamKills() != null && pgs.getTeamKills() > 0) {
                double k = pgs.getKills() != null ? pgs.getKills() : 0;
                double a = pgs.getAssists() != null ? pgs.getAssists() : 0;
                sumKillParticipation += (k + a) / pgs.getTeamKills();
            }

            Long gameId = pgs.getGame().getId();
            String side = pgs.getSide();

            // CS Share Post 15
            if (pgs.getTotalCs() != null && pgs.getCsAt15() != null) {
                int myCsPost15 = pgs.getTotalCs() - pgs.getCsAt15();
                GameSideTotals sideTotals = gameSideTotals.get(gameId).get(side);
                if (sideTotals != null && sideTotals.totalCsPost15 > 0) {
                    sumCsSharePost15 += (double) myCsPost15 / sideTotals.totalCsPost15;
                }
            }

            GameTotals gTotals = gameTotals.get(gameId);
            if (gTotals != null) {
                if (pgs.getMonsterKills() != null && gTotals.totalJungleCs > 0) {
                    sumJungleShare += (double) pgs.getMonsterKills() / gTotals.totalJungleCs;
                }
                if (pgs.getMinionKills() != null && gTotals.totalLaneCs > 0) {
                    sumLaneShare += (double) pgs.getMinionKills() / gTotals.totalLaneCs;
                }
            }

            Game game = pgs.getGame();
            if (game != null) {
                double duration = game.getDurationSeconds() / 60.0;
                totalDurationMinutes += duration;
                if (game.getWinnerTeam() != null && stats.getPlayer().getTeam() != null &&
                        game.getWinnerTeam().getId().equals(stats.getPlayer().getTeam().getId())) {
                    wins++;
                }
            }
        }

        stats.setKills(kills);
        stats.setDeaths(deaths);
        stats.setAssists(assists);
        stats.setWinRate(gamesPlayed > 0 ? (double) wins / gamesPlayed * 100 : 0);
        stats.setKda(deaths > 0 ? (double) (kills + assists) / deaths : (kills + assists));
        stats.setFirstBloodRate(gamesPlayed > 0 ? (double) firstBloodKills / gamesPlayed * 100 : 0);

        stats.setGoldDiff10(countGoldDiff10 > 0 ? sumGoldDiff10 / countGoldDiff10 : 0);
        stats.setXpDiff10(countXpDiff10 > 0 ? sumXpDiff10 / countXpDiff10 : 0);
        stats.setCsDiff10(countCsDiff10 > 0 ? sumCsDiff10 / countCsDiff10 : 0);

        stats.setCspm(totalDurationMinutes > 0 ? totalCs / totalDurationMinutes : 0);
        stats.setDpm(totalDurationMinutes > 0 ? totalDamage / totalDurationMinutes : 0);
        stats.setTotalDamagePerGame(gamesPlayed > 0 ? (double) totalDamage / gamesPlayed : 0);
        stats.setEarnedGoldPerMinute(totalDurationMinutes > 0 ? totalGold / totalDurationMinutes : 0);

        stats.setDamageShare(gamesPlayed > 0 ? sumDamageShare / gamesPlayed * 100 : 0);
        stats.setGoldShare(gamesPlayed > 0 ? sumGoldShare / gamesPlayed * 100 : 0);

        stats.setKillShare(gamesPlayed > 0 ? sumKillShare / gamesPlayed * 100 : 0);
        stats.setDeathShare(gamesPlayed > 0 ? sumDeathShare / gamesPlayed * 100 : 0);
        stats.setKillParticipation(gamesPlayed > 0 ? sumKillParticipation / gamesPlayed * 100 : 0);
        stats.setCsSharePost15(gamesPlayed > 0 ? sumCsSharePost15 / gamesPlayed * 100 : 0);
        stats.setJungleShare(gamesPlayed > 0 ? sumJungleShare / gamesPlayed * 100 : 0);
        stats.setLaneShare(gamesPlayed > 0 ? sumLaneShare / gamesPlayed * 100 : 0);

        stats.setWardsPerMinute(totalDurationMinutes > 0 ? totalWardsPlaced / totalDurationMinutes : 0);
        stats.setWardsClearedPerMinute(totalDurationMinutes > 0 ? totalWardsKilled / totalDurationMinutes : 0);
        stats.setControlWardsPerMinute(totalDurationMinutes > 0 ? totalControlWards / totalDurationMinutes : 0);
    }

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
                        .jungleShare(stat.getJungleShare())
                        .laneShare(stat.getLaneShare())
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
