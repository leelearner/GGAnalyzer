package com.gganalyzer.service;

import com.gganalyzer.dto.*;
import com.gganalyzer.model.Game;
import com.gganalyzer.model.League;
import com.gganalyzer.model.Match;
import com.gganalyzer.model.Player;
import com.gganalyzer.model.PlayerGameStats;
import com.gganalyzer.model.PlayerStats;
import com.gganalyzer.model.Stage;
import com.gganalyzer.model.Team;
import com.gganalyzer.model.TeamGameStats;
import com.gganalyzer.model.TeamStats;
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
    private TeamStatsRepository teamStatsRepository;
    @Autowired
    private TeamGameStatsRepository teamGameStatsRepository;

    @Autowired
    private PlayerGameStatsRepository playerGameStatsRepository;

    private Object lock = new Object();
    private Object teamStatsLock = new Object();

    public List<TeamStatsDTO> getTeamStatsByStage(String stageName) {
        Stage stage = stageRepository.findByName(stageName)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + stageName));
        List<TeamStatsDTO> stats = teamStatsRepository.findByStage(stage).stream()
                .map(teamStats -> TeamStatsDTO.builder()
                        .team(teamStats.getTeam())
                        .stage(teamStats.getStage())
                        .league(teamStats.getLeague())
                        .gamesPlayed(teamStats.getGamesPlayed())
                        .wins(teamStats.getWins())
                        .losses(teamStats.getLosses())
                        .averageGameTime(teamStats.getAverageGameTime())
                        .totalKills(teamStats.getTotalKills())
                        .totalDeaths(teamStats.getTotalDeaths())
                        .kd(teamStats.getKd())
                        .combinedKillsPerMinute(teamStats.getCombinedKillsPerMinute())
                        .goldPercentRating(teamStats.getGoldPercentRating())
                        .goldSpentPerDiff(teamStats.getGoldSpentPerDiff())
                        .gd15(teamStats.getGd15())
                        .firstBloodPercent(teamStats.getFirstBloodPercent())
                        .firstTowerPercent(teamStats.getFirstTowerPercent())
                        .firstThreeTowersPercent(teamStats.getFirstThreeTowersPercent())
                        .ppg(teamStats.getPpg())
                        .heraldPercent(teamStats.getHeraldPercent())
                        .grubPercent(teamStats.getGrubPercent())
                        .firstDragonPercent(teamStats.getFirstDragonPercent())
                        .dragonPercent(teamStats.getDragonPercent())
                        .elderDragonPercent(teamStats.getElderDragonPercent())
                        .firstBaronPercent(teamStats.getFirstBaronPercent())
                        .baronPercent(teamStats.getBaronPercent())
                        .lanePercent(teamStats.getLanePercent())
                        .junglePercent(teamStats.getJunglePercent())
                        .wardsPerMinute(teamStats.getWardsPerMinute())
                        .controlWardsPerMinute(teamStats.getControlWardsPerMinute())
                        .wardsClearedPerMinute(teamStats.getWardsClearedPerMinute())
                        .build())
                .collect(Collectors.toList());
        return stats;
    }

    @Transactional
    public void calculatePlayerStats(Stage stage) {
        List<Match> matches = matchRepository.findByStage(stage);
        if (matches.isEmpty())
            return;

        // Collect all games in this stage
        List<Game> games = new ArrayList<>();
        for (Match match : matches) {
            games.addAll(match.getGames());
        }
        if (games.isEmpty())
            return;

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

            if (pgs.getTotalCs() != null && pgs.getCsAt15() != null) {
                sideTotals.totalCsPost15 += (pgs.getTotalCs() - pgs.getCsAt15());
            }

            if (pgs.getMonsterKills() != null) {
                gTotals.totalJungleCs += pgs.getMonsterKills();
            }

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

    private static class GameSideTotals {
        Integer totalCsPost15 = 0;
    }

    private static class GameTotals {
        Integer totalJungleCs = 0;
        Integer totalLaneCs = 0;
    }

    private void updatePlayerStats(PlayerStats stats, List<PlayerGameStats> pgsList, List<Game> stageGames,
            Map<Long, Map<String, GameSideTotals>> gameSideTotals, Map<Long, GameTotals> gameTotals) {
        Integer gamesPlayed = pgsList.size();
        stats.setGamesPlayed(gamesPlayed);

        // Basic sums
        Integer kills = 0;
        Integer deaths = 0;
        Integer assists = 0;
        Long totalDamage = 0L;
        Long totalGold = 0L;
        Integer totalCs = 0;
        Integer totalWardsPlaced = 0;
        Integer totalWardsKilled = 0;
        Integer totalControlWards = 0;
        Double totalDurationMinutes = 0.0;

        Integer wins = 0;
        Integer firstBloodKills = 0;

        Double sumGoldDiff10 = 0.0;
        Integer countGoldDiff10 = 0;
        Double sumXpDiff10 = 0.0;
        Integer countXpDiff10 = 0;
        Double sumCsDiff10 = 0.0;
        Integer countCsDiff10 = 0;

        Double sumDamageShare = 0.0;
        Double sumGoldShare = 0.0;
        Double sumVisionScore = 0.0;

        Double sumKillShare = 0.0;
        Double sumDeathShare = 0.0;
        Double sumKillParticipation = 0.0;
        Double sumCsSharePost15 = 0.0;
        Double sumJungleShare = 0.0;
        Double sumLaneShare = 0.0;

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
                Double k = pgs.getKills() != null ? pgs.getKills().doubleValue() : 0.0;
                Double a = pgs.getAssists() != null ? pgs.getAssists().doubleValue() : 0.0;
                sumKillParticipation += (k + a) / pgs.getTeamKills();
            }

            Long gameId = pgs.getGame().getId();
            String side = pgs.getSide();

            // CS Share Post 15
            if (pgs.getTotalCs() != null && pgs.getCsAt15() != null) {
                Integer myCsPost15 = pgs.getTotalCs() - pgs.getCsAt15();
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
                Double duration = game.getDurationSeconds() / 60.0;
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

    @Transactional
    public void calculateTeamStats(Stage stage) {
        List<Match> matches = matchRepository.findByStage(stage);
        if (matches.isEmpty())
            return;

        League league = matches.get(0).getLeague();

        List<Game> games = matches.stream()
                .flatMap(m -> m.getGames().stream())
                .collect(Collectors.toList());
        if (games.isEmpty())
            return;

        List<TeamGameStats> allTeamGameStats = new ArrayList<>();
        List<PlayerGameStats> allPlayerGameStats = new ArrayList<>();

        for (Game game : games) {
            allTeamGameStats.addAll(teamGameStatsRepository.findByGame(game));
            allPlayerGameStats.addAll(playerGameStatsRepository.findByGame(game));
        }

        // Map GameId -> List<TeamGameStats> (for opponent lookup)
        Map<Long, List<TeamGameStats>> gameTeamStatsMap = allTeamGameStats.stream()
                .collect(Collectors.groupingBy(tgs -> tgs.getGame().getId()));

        // Map GameId -> Side -> List<PlayerGameStats>
        Map<Long, Map<String, List<PlayerGameStats>>> gameSidePlayerStats = new HashMap<>();
        for (PlayerGameStats pgs : allPlayerGameStats) {
            gameSidePlayerStats
                    .computeIfAbsent(pgs.getGame().getId(), k -> new HashMap<>())
                    .computeIfAbsent(pgs.getSide(), k -> new ArrayList<>())
                    .add(pgs);
        }

        // Group by Team
        Map<Team, List<TeamGameStats>> teamStatsMap = allTeamGameStats.stream()
                .collect(Collectors.groupingBy(TeamGameStats::getTeam));

        for (Map.Entry<Team, List<TeamGameStats>> entry : teamStatsMap.entrySet()) {
            Team team = entry.getKey();
            List<TeamGameStats> tgsList = entry.getValue();

            synchronized (teamStatsLock) {
                TeamStats stats = teamStatsRepository.findByTeamAndStage(team, stage)
                        .orElse(TeamStats.builder().team(team).stage(stage).league(league).build());
                updateTeamStats(stats, tgsList, gameTeamStatsMap, gameSidePlayerStats);
                teamStatsRepository.save(stats);
            }
        }
    }

    private void updateTeamStats(TeamStats stats, List<TeamGameStats> tgsList,
            Map<Long, List<TeamGameStats>> gameTeamStatsMap,
            Map<Long, Map<String, List<PlayerGameStats>>> gameSidePlayerStats) {
        Integer gamesPlayed = tgsList.size();
        stats.setGamesPlayed(gamesPlayed);

        Integer wins = 0;
        Integer losses = 0;
        Double totalDuration = 0.0;
        Integer totalKills = 0;
        Integer totalDeaths = 0;

        Integer firstBloodCount = 0;
        Integer firstTowerCount = 0;
        Integer firstThreeTowersCount = 0;
        Integer firstDragonCount = 0;
        Integer firstBaronCount = 0;

        Integer totalHeralds = 0;
        Integer totalOppHeralds = 0;
        Integer totalGrubs = 0;
        Integer totalOppGrubs = 0;
        Integer totalDragons = 0;
        Integer totalOppDragons = 0;
        Integer totalElders = 0;
        Integer totalOppElders = 0;
        Integer totalBarons = 0;
        Integer totalOppBarons = 0;

        Double sumGd15 = 0.0;
        Integer countGd15 = 0;

        Integer totalWardsPlaced = 0;
        Integer totalWardsKilled = 0;
        Integer totalControlWards = 0;

        Double totalLaneCs = 0.0;
        Double totalJungleCs = 0.0;

        Long totalGold = 0L;
        Long totalOppGold = 0L;

        for (TeamGameStats tgs : tgsList) {
            if (Boolean.TRUE.equals(tgs.getWin()))
                wins++;
            else
                losses++;

            Game game = tgs.getGame();
            Double duration = game.getDurationSeconds() / 60.0;
            totalDuration += duration;

            totalKills += tgs.getKills() != null ? tgs.getKills() : 0;
            totalDeaths += tgs.getDeaths() != null ? tgs.getDeaths() : 0;
            totalGold += tgs.getTotalGold() != null ? tgs.getTotalGold() : 0;

            if (Boolean.TRUE.equals(tgs.getFirstBlood()))
                firstBloodCount++;
            if (Boolean.TRUE.equals(tgs.getFirstTower()))
                firstTowerCount++;
            if (Boolean.TRUE.equals(tgs.getFirstToThreeTowers()))
                firstThreeTowersCount++;
            if (Boolean.TRUE.equals(tgs.getFirstDragon()))
                firstDragonCount++;
            if (Boolean.TRUE.equals(tgs.getFirstBaron()))
                firstBaronCount++;

            totalHeralds += tgs.getHeralds() != null ? tgs.getHeralds() : 0;
            totalGrubs += tgs.getVoidGrubs() != null ? tgs.getVoidGrubs() : 0;
            totalDragons += tgs.getDragons() != null ? tgs.getDragons() : 0;
            totalElders += tgs.getElders() != null ? tgs.getElders() : 0;
            totalBarons += tgs.getBarons() != null ? tgs.getBarons() : 0;

            // Opponent stats
            List<TeamGameStats> gameStats = gameTeamStatsMap.get(game.getId());
            if (gameStats != null) {
                for (TeamGameStats oppTgs : gameStats) {
                    if (!oppTgs.getTeam().getId().equals(tgs.getTeam().getId())) {
                        totalOppHeralds += oppTgs.getHeralds() != null ? oppTgs.getHeralds() : 0;
                        totalOppGrubs += oppTgs.getVoidGrubs() != null ? oppTgs.getVoidGrubs() : 0;
                        totalOppDragons += oppTgs.getDragons() != null ? oppTgs.getDragons() : 0;
                        totalOppElders += oppTgs.getElders() != null ? oppTgs.getElders() : 0;
                        totalOppBarons += oppTgs.getBarons() != null ? oppTgs.getBarons() : 0;
                        totalOppGold += oppTgs.getTotalGold() != null ? oppTgs.getTotalGold() : 0;
                    }
                }
            }

            // Player stats for this team in this game
            Map<String, List<PlayerGameStats>> sideStats = gameSidePlayerStats.get(game.getId());
            if (sideStats != null) {
                List<PlayerGameStats> pgsList = sideStats.get(tgs.getSide());
                if (pgsList != null) {
                    Double gameGd15 = 0.0;
                    Boolean hasGd15 = false;
                    for (PlayerGameStats pgs : pgsList) {
                        if (pgs.getGoldDiffAt15() != null) {
                            gameGd15 += pgs.getGoldDiffAt15();
                            hasGd15 = true;
                        }
                        totalWardsPlaced += pgs.getWardsPlaced() != null ? pgs.getWardsPlaced() : 0;
                        totalWardsKilled += pgs.getWardsKilled() != null ? pgs.getWardsKilled() : 0;
                        totalControlWards += pgs.getControlWardsBought() != null ? pgs.getControlWardsBought() : 0;

                        totalLaneCs += pgs.getMinionKills() != null ? pgs.getMinionKills() : 0;
                        totalJungleCs += pgs.getMonsterKills() != null ? pgs.getMonsterKills() : 0;
                    }
                    if (hasGd15) {
                        sumGd15 += gameGd15;
                        countGd15++;
                    }
                }
            }
        }

        stats.setWins(wins);
        stats.setLosses(losses);
        stats.setAverageGameTime(gamesPlayed > 0 ? totalDuration / gamesPlayed : 0);
        stats.setTotalKills(totalKills);
        stats.setTotalDeaths(totalDeaths);
        stats.setKd(totalDeaths > 0 ? (double) totalKills / totalDeaths : totalKills);
        stats.setCombinedKillsPerMinute(totalDuration > 0 ? (double) (totalKills + totalDeaths) / totalDuration : 0);

        stats.setFirstBloodPercent(gamesPlayed > 0 ? (double) firstBloodCount / gamesPlayed * 100 : 0);
        stats.setFirstTowerPercent(gamesPlayed > 0 ? (double) firstTowerCount / gamesPlayed * 100 : 0);
        stats.setFirstThreeTowersPercent(gamesPlayed > 0 ? (double) firstThreeTowersCount / gamesPlayed * 100 : 0);
        stats.setFirstDragonPercent(gamesPlayed > 0 ? (double) firstDragonCount / gamesPlayed * 100 : 0);
        stats.setFirstBaronPercent(gamesPlayed > 0 ? (double) firstBaronCount / gamesPlayed * 100 : 0);

        stats.setHeraldPercent(
                (totalHeralds + totalOppHeralds) > 0 ? (double) totalHeralds / (totalHeralds + totalOppHeralds) * 100
                        : 0);
        stats.setGrubPercent(
                (totalGrubs + totalOppGrubs) > 0 ? (double) totalGrubs / (totalGrubs + totalOppGrubs) * 100 : 0);
        stats.setDragonPercent(
                (totalDragons + totalOppDragons) > 0 ? (double) totalDragons / (totalDragons + totalOppDragons) * 100
                        : 0);
        stats.setElderDragonPercent(
                (totalElders + totalOppElders) > 0 ? (double) totalElders / (totalElders + totalOppElders) * 100 : 0);
        stats.setBaronPercent(
                (totalBarons + totalOppBarons) > 0 ? (double) totalBarons / (totalBarons + totalOppBarons) * 100 : 0);

        stats.setGd15(countGd15 > 0 ? sumGd15 / countGd15 : 0);

        stats.setWardsPerMinute(totalDuration > 0 ? totalWardsPlaced / totalDuration : 0);
        stats.setControlWardsPerMinute(totalDuration > 0 ? totalControlWards / totalDuration : 0);
        stats.setWardsClearedPerMinute(totalDuration > 0 ? totalWardsKilled / totalDuration : 0);

        double totalCs = totalLaneCs + totalJungleCs;
        stats.setLanePercent(totalCs > 0 ? totalLaneCs / totalCs * 100 : 0);
        stats.setJunglePercent(totalCs > 0 ? totalJungleCs / totalCs * 100 : 0);

        stats.setGoldPercentRating(
                (totalGold + totalOppGold) > 0 ? (double) totalGold / (totalGold + totalOppGold) * 100 : 0);
        stats.setGoldSpentPerDiff(0.0); // Placeholder
        stats.setPpg(0.0); // Placeholder
    }

    public List<StandingsDTO> getStandings(String stageName) {
        if (stageName == null) {
            return new ArrayList<>();
        }
        Stage stage = stageRepository.findByName(stageName).orElse(null);
        if (stage == null) {
            return new ArrayList<>();
        }
        List<Match> matches = matchRepository.findByStage(stage);
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

        List<StandingsDTO> standings = standingsMap.values().stream()
                .sorted((a, b) -> {
                    if (b.wins != a.wins)
                        return b.wins - a.wins;
                    return b.pointDiff - a.pointDiff;
                })
                .map(s -> {
                    Integer totalGames = s.wins + s.losses;
                    String winRate = totalGames == 0 ? "0%"
                            : String.format("%.1f%%", (double) s.wins / totalGames * 100);
                    return StandingsDTO.builder()
                            .rank(0) // Will set rank after collecting
                            .teamName(s.team.getName())
                            .teamAcronym(s.team.getAcronym())
                            .team(s.team)
                            .wins(s.wins)
                            .losses(s.losses)
                            .pointDiff(s.pointDiff)
                            .winRate(winRate)
                            .streak("0W") // Placeholder
                            .build();
                })
                .collect(Collectors.toList());

        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setRank(i + 1);
        }
        return standings;
    }

    private static class TeamStanding {
        Team team;
        Integer wins = 0;
        Integer losses = 0;
        Integer pointDiff = 0;

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
        return teamStatsRepository.findAll().stream()
                .map(stat -> TeamStatsDTO.builder()
                        .team(stat.getTeam())
                        .stage(stat.getStage())
                        .league(stat.getLeague())
                        .gamesPlayed(stat.getGamesPlayed())
                        .wins(stat.getWins())
                        .losses(stat.getLosses())
                        .averageGameTime(stat.getAverageGameTime())
                        .totalKills(stat.getTotalKills())
                        .totalDeaths(stat.getTotalDeaths())
                        .kd(stat.getKd())
                        .combinedKillsPerMinute(stat.getCombinedKillsPerMinute())
                        .goldPercentRating(stat.getGoldPercentRating())
                        .goldSpentPerDiff(stat.getGoldSpentPerDiff())
                        .gd15(stat.getGd15())
                        .firstBloodPercent(stat.getFirstBloodPercent())
                        .firstTowerPercent(stat.getFirstTowerPercent())
                        .firstThreeTowersPercent(stat.getFirstThreeTowersPercent())
                        .ppg(stat.getPpg())
                        .heraldPercent(stat.getHeraldPercent())
                        .grubPercent(stat.getGrubPercent())
                        .firstDragonPercent(stat.getFirstDragonPercent())
                        .dragonPercent(stat.getDragonPercent())
                        .elderDragonPercent(stat.getElderDragonPercent())
                        .firstBaronPercent(stat.getFirstBaronPercent())
                        .baronPercent(stat.getBaronPercent())
                        .lanePercent(stat.getLanePercent())
                        .junglePercent(stat.getJunglePercent())
                        .wardsPerMinute(stat.getWardsPerMinute())
                        .controlWardsPerMinute(stat.getControlWardsPerMinute())
                        .wardsClearedPerMinute(stat.getWardsClearedPerMinute())
                        .build())
                .collect(Collectors.toList());
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
