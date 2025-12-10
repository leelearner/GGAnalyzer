package com.gganalyzer.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gganalyzer.repository.GameRepository;
import com.gganalyzer.repository.PlayerGameStatsRepository;
import com.gganalyzer.repository.PlayerRepository;
import com.gganalyzer.repository.ChampionRepository;
import com.gganalyzer.repository.TeamRepository;
import com.gganalyzer.repository.TeamGameStatsRepository;
import com.gganalyzer.model.Champion;
import com.gganalyzer.model.Game;
import com.gganalyzer.model.Match;
import com.gganalyzer.model.Player;
import com.gganalyzer.model.PlayerGameStats;
import com.gganalyzer.model.Team;
import com.gganalyzer.model.TeamGameStats;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private MatchService matchService;
    @Autowired
    private PlayerGameStatsRepository playerGameStatsRepository;
    @Autowired
    private TeamGameStatsRepository teamGameStatsRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private ChampionRepository championRepository;
    @Autowired
    private TeamRepository teamRepository;

    private Object playerLock = new Object();

    public void asyncCreateGames(String[] teamA, String[] teamB) {
        saveGames(teamA, teamB);
    }

    public void createGameWithStats(String[] teamA, String[] teamB, java.util.List<String[]> playerRows) {
        Game game = saveGames(teamA, teamB);
        if (game != null) {
            savePlayerGameStats(game, playerRows);
            saveTeamGameStats(game, teamA);
            saveTeamGameStats(game, teamB);
        }
    }

    private void saveTeamGameStats(Game game, String[] teamRow) {
        try {
            String teamName = teamRow[15];
            Team team = findOrCreateTeam(teamName);

            // Check if stats already exist for this team in this game
            if (teamGameStatsRepository.existsByGameAndTeam(game, team)) {
                return;
            }

            TeamGameStats stats = TeamGameStats.builder()
                    .game(game)
                    .team(team)
                    .side(teamRow[11])
                    .win("1".equals(teamRow[29]))
                    .kills(parseInt(teamRow[30]))
                    .deaths(parseInt(teamRow[31]))
                    .assists(parseInt(teamRow[32]))
                    .totalGold(parseInt(teamRow[91]))
                    .earnedGold(parseInt(teamRow[92]))
                    .towers(parseInt(teamRow[70]))
                    .inhibitors(parseInt(teamRow[76]))
                    .barons(parseInt(teamRow[65]))
                    .dragons(parseInt(teamRow[46]))
                    .elders(parseInt(teamRow[57]))
                    .heralds(parseInt(teamRow[60]))
                    .voidGrubs(parseInt(teamRow[62]))
                    .firstBlood(parseBoolean(teamRow[39]))
                    .firstTower(parseBoolean(teamRow[69]))
                    .firstDragon(parseBoolean(teamRow[45]))
                    .firstBaron(parseBoolean(teamRow[64]))
                    .firstHerald(parseBoolean(teamRow[59]))
                    .firstToThreeTowers(parseBoolean(teamRow[73]))
                    .ban1(teamRow[18])
                    .ban2(teamRow[19])
                    .ban3(teamRow[20])
                    .ban4(teamRow[21])
                    .ban5(teamRow[22])
                    .pick1(teamRow[23])
                    .pick2(teamRow[24])
                    .pick3(teamRow[25])
                    .pick4(teamRow[26])
                    .pick5(teamRow[27])
                    .build();

            teamGameStatsRepository.save(stats);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Object gameLock = new Object();

    /**
     * Saves games to the database.
     * 
     * @param teamA Array of team A data.
     * @param teamB Array of team B data.
     * @return The saved Game entity
     */
    @SuppressWarnings("null")
    private Game saveGames(String[] teamA, String[] teamB) {
        String gameId = teamA[0].trim();
        if (teamA[15].compareTo(teamB[15]) > 0) {
            String[] temp = teamA;
            teamA = teamB;
            teamB = temp;
        }
        String date = teamA[7].split(" ")[0].trim();
        Match match = matchService.findMatchByDateAndTeams(date, teamA[15], teamB[15]);
        if (match == null)
            return null;

        Integer gameNumber = Integer.parseInt(teamA[8].trim());
        Long durationSeconds = Long.parseLong(teamA[28].trim());
        Team winnerTeam = teamA[29].trim().equals("1") ? match.getTeamA() : match.getTeamB();
        String patchVersion = teamA[9].trim();
        Game newGame = Game.builder()
                .match(match)
                .gameNumber(gameNumber)
                .durationSeconds(durationSeconds)
                .winnerTeam(winnerTeam)
                .patchVersion(patchVersion)
                .gameId(gameId)
                .build();
        synchronized (gameLock) {
            Optional<Game> existing = gameRepository.findByGameId(gameId);
            if (existing.isPresent()) {
                return existing.get();
            }
            Game savedGame = gameRepository.save(newGame);
            if (winnerTeam == match.getTeamA()) {
                matchService.increaseTeamAScore(match);
            } else {
                matchService.increaseTeamBScore(match);
            }
            return savedGame;
        }
    }

    private void savePlayerGameStats(Game game, java.util.List<String[]> playerRows) {
        for (String[] row : playerRows) {
            try {
                String playerName = row[13]; // playername
                String championName = row[17]; // champion

                String teamName = row[15];
                Team team = findOrCreateTeam(teamName);

                Player player;
                synchronized (playerLock) {
                    player = playerRepository.findByHandle(playerName).orElse(null);
                    if (player == null) {
                        player = Player.builder()
                                .handle(playerName)
                                .role(row[12]) // position
                                .team(team)
                                .build();
                        player = playerRepository.save(player);
                    }
                }

                if (playerGameStatsRepository.existsByGameAndPlayer(game, player)) {
                    continue;
                }

                Champion champion = findOrCreateChampion(championName);

                PlayerGameStats stats = PlayerGameStats.builder()
                        .game(game)
                        .player(player)
                        .champion(champion)
                        .side(row[11])
                        .position(row[12])
                        .participantId(parseInt(row[10]))
                        .dataCompleteness(row[1])
                        .url(row[2])
                        .result(parseInt(row[29]))
                        .gameLength(parseDouble(row[28]))
                        .kills(parseInt(row[30]))
                        .deaths(parseInt(row[31]))
                        .assists(parseInt(row[32]))
                        .teamKills(parseInt(row[33]))
                        .teamDeaths(parseInt(row[34]))
                        .doubleKills(parseInt(row[35]))
                        .tripleKills(parseInt(row[36]))
                        .quadraKills(parseInt(row[37]))
                        .pentaKills(parseInt(row[38]))
                        .firstBlood(parseBoolean(row[39]))
                        .firstBloodKill(parseBoolean(row[40]))
                        .firstBloodAssist(parseBoolean(row[41]))
                        .firstBloodVictim(parseBoolean(row[42]))
                        .teamKpm(parseDouble(row[43]))
                        .ckpm(parseDouble(row[44]))
                        .firstDragon(parseInt(row[45]))
                        .dragons(parseInt(row[46]))
                        .oppDragons(parseInt(row[47]))
                        .elementalDrakes(parseInt(row[48]))
                        .oppElementalDrakes(parseInt(row[49]))
                        .infernals(parseInt(row[50]))
                        .mountains(parseInt(row[51]))
                        .clouds(parseInt(row[52]))
                        .oceans(parseInt(row[53]))
                        .chemtechs(parseInt(row[54]))
                        .hextechs(parseInt(row[55]))
                        .dragonsTypeUnknown(parseInt(row[56]))
                        .elders(parseInt(row[57]))
                        .oppElders(parseInt(row[58]))
                        .firstHerald(parseInt(row[59]))
                        .heralds(parseInt(row[60]))
                        .oppHeralds(parseInt(row[61]))
                        .voidGrubs(parseInt(row[62]))
                        .oppVoidGrubs(parseInt(row[63]))
                        .firstBaron(parseInt(row[64]))
                        .barons(parseInt(row[65]))
                        .oppBarons(parseInt(row[66]))
                        .atakhans(parseInt(row[67]))
                        .oppAtakhans(parseInt(row[68]))
                        .firstTower(parseInt(row[69]))
                        .towers(parseInt(row[70]))
                        .oppTowers(parseInt(row[71]))
                        .firstMidTower(parseInt(row[72]))
                        .firstToThreeTowers(parseInt(row[73]))
                        .turretPlates(parseInt(row[74]))
                        .oppTurretPlates(parseInt(row[75]))
                        .inhibitors(parseInt(row[76]))
                        .oppInhibitors(parseInt(row[77]))
                        .damageToChampions(parseDouble(row[78]))
                        .dpm(parseDouble(row[79]))
                        .damageShare(parseDouble(row[80]))
                        .damageTakenPerMinute(parseDouble(row[81]))
                        .damageMitigatedPerMinute(parseDouble(row[82]))
                        .damageToTowers(parseDouble(row[83]))
                        .wardsPlaced(parseInt(row[84]))
                        .wpm(parseDouble(row[85]))
                        .wardsKilled(parseInt(row[86]))
                        .wcpm(parseDouble(row[87]))
                        .controlWardsBought(parseInt(row[88]))
                        .visionScore(parseDouble(row[89]))
                        .vspm(parseDouble(row[90]))
                        .totalGold(parseInt(row[91]))
                        .earnedGold(parseInt(row[92]))
                        .earnedGpm(parseDouble(row[93]))
                        .earnedGoldShare(parseDouble(row[94]))
                        .goldSpent(parseInt(row[95]))
                        .gspd(parseDouble(row[96]))
                        .gpr(parseDouble(row[97]))
                        .totalCs(parseInt(row[98]))
                        .minionKills(parseInt(row[99]))
                        .monsterKills(parseInt(row[100]))
                        .monsterKillsOwnJungle(parseInt(row[101]))
                        .monsterKillsEnemyJungle(parseInt(row[102]))
                        .cspm(parseDouble(row[103]))
                        .goldAt10(parseInt(row[104]))
                        .xpAt10(parseInt(row[105]))
                        .csAt10(parseInt(row[106]))
                        .oppGoldAt10(parseInt(row[107]))
                        .oppXpAt10(parseInt(row[108]))
                        .oppCsAt10(parseInt(row[109]))
                        .goldDiffAt10(parseInt(row[110]))
                        .xpDiffAt10(parseInt(row[111]))
                        .csDiffAt10(parseInt(row[112]))
                        .killsAt10(parseInt(row[113]))
                        .assistsAt10(parseInt(row[114]))
                        .deathsAt10(parseInt(row[115]))
                        .oppKillsAt10(parseInt(row[116]))
                        .oppAssistsAt10(parseInt(row[117]))
                        .oppDeathsAt10(parseInt(row[118]))
                        .goldAt15(parseInt(row[119]))
                        .xpAt15(parseInt(row[120]))
                        .csAt15(parseInt(row[121]))
                        .oppGoldAt15(parseInt(row[122]))
                        .oppXpAt15(parseInt(row[123]))
                        .oppCsAt15(parseInt(row[124]))
                        .goldDiffAt15(parseInt(row[125]))
                        .xpDiffAt15(parseInt(row[126]))
                        .csDiffAt15(parseInt(row[127]))
                        .killsAt15(parseInt(row[128]))
                        .assistsAt15(parseInt(row[129]))
                        .deathsAt15(parseInt(row[130]))
                        .oppKillsAt15(parseInt(row[131]))
                        .oppAssistsAt15(parseInt(row[132]))
                        .oppDeathsAt15(parseInt(row[133]))
                        .goldAt20(parseInt(row[134]))
                        .xpAt20(parseInt(row[135]))
                        .csAt20(parseInt(row[136]))
                        .oppGoldAt20(parseInt(row[137]))
                        .oppXpAt20(parseInt(row[138]))
                        .oppCsAt20(parseInt(row[139]))
                        .goldDiffAt20(parseInt(row[140]))
                        .xpDiffAt20(parseInt(row[141]))
                        .csDiffAt20(parseInt(row[142]))
                        .killsAt20(parseInt(row[143]))
                        .assistsAt20(parseInt(row[144]))
                        .deathsAt20(parseInt(row[145]))
                        .oppKillsAt20(parseInt(row[146]))
                        .oppAssistsAt20(parseInt(row[147]))
                        .oppDeathsAt20(parseInt(row[148]))
                        .goldAt25(parseInt(row[149]))
                        .xpAt25(parseInt(row[150]))
                        .csAt25(parseInt(row[151]))
                        .oppGoldAt25(parseInt(row[152]))
                        .oppXpAt25(parseInt(row[153]))
                        .oppCsAt25(parseInt(row[154]))
                        .goldDiffAt25(parseInt(row[155]))
                        .xpDiffAt25(parseInt(row[156]))
                        .csDiffAt25(parseInt(row[157]))
                        .killsAt25(parseInt(row[158]))
                        .assistsAt25(parseInt(row[159]))
                        .deathsAt25(parseInt(row[160]))
                        .oppKillsAt25(parseInt(row[161]))
                        .oppAssistsAt25(parseInt(row[162]))
                        .oppDeathsAt25(parseInt(row[163]))
                        .build();

                playerGameStatsRepository.save(stats);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Integer parseInt(String value) {
        try {
            if (value == null || value.trim().isEmpty())
                return null;
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        try {
            if (value == null || value.trim().isEmpty())
                return null;
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        return "1".equals(value.trim());
    }

    private Champion findOrCreateChampion(String championName) {
        if (championName == null || championName.isEmpty())
            return null;
        return championRepository.findByName(championName)
                .orElseGet(() -> {
                    Champion newChampion = Champion.builder()
                            .name(championName)
                            .build();
                    return championRepository.save(newChampion);
                });
    }

    private Team findOrCreateTeam(String teamName) {
        if (teamName == null || teamName.isEmpty())
            return null;
        return teamRepository.findByName(teamName)
                .orElseGet(() -> {
                    String acronym = generateAcronym(teamName);
                    int counter = 1;
                    String originalAcronym = acronym;
                    while (teamRepository.findByAcronym(acronym).isPresent()) {
                        acronym = originalAcronym + counter;
                        counter++;
                    }
                    Team newTeam = Team.builder()
                            .name(teamName)
                            .acronym(acronym)
                            .build();
                    return teamRepository.save(newTeam);
                });
    }

    private String generateAcronym(String teamName) {
        String[] parts = teamName.split("\\s+");
        if (parts.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    sb.append(part.charAt(0));
                }
            }
            String acronym = sb.toString().toUpperCase();
            if (acronym.length() >= 2)
                return acronym;
        }

        return teamName.length() > 3 ? teamName.substring(0, 3).toUpperCase() : teamName.toUpperCase();
    }
}
