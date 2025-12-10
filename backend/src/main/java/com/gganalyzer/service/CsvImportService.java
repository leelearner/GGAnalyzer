package com.gganalyzer.service;

import com.gganalyzer.model.AppConfig;
import com.gganalyzer.model.Champion;
import com.gganalyzer.model.League;
import com.gganalyzer.model.Match;
import com.gganalyzer.model.Stage;
import com.gganalyzer.model.Team;
import com.gganalyzer.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class CsvImportService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AppConfigRepository appConfigRepository;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private ChampionRepository championRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private GameService gameService;

    private final Object teamLock = new Object();
    private final Object matchLock = new Object();
    private final Object stageLock = new Object();
    // private final ReentrantReadWriteLock stageLock = new
    // ReentrantReadWriteLock();
    // private final ReadLock readStageLock = stageLock.readLock();
    // private final WriteLock writeStageLock = stageLock.writeLock();

    @Transactional
    public void importPlayerStats(String filePath) {
        // Deprecated: Player stats are now calculated from match data
        System.out.println("Skipping direct Player Stats CSV import. Stats will be calculated from match data.");
    }

    private String[] parseCsvLine(String line) {
        // Simple CSV parser handling quotes
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    public List<String> splitMatchesData(String filePath) {
        try {
            String currentHash = calculateFileHash(filePath);
            String configKey = "hash_" + new File(filePath).getName();
            Optional<AppConfig> configOpt = appConfigRepository.findById(configKey);
            if (configOpt.isPresent() && configOpt.get().getConfigValue().equals(currentHash)) {
                System.out.println("File " + filePath + " has not changed. Skipping import.");
                return new ArrayList<>();
            }
            AppConfig config = configOpt.orElse(AppConfig.builder().configKey(configKey).build());
            config.setConfigValue(currentHash);
            appConfigRepository.save(config);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> leagueStages = new ArrayList<>();
        List<String> champions = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        Map<String, BufferedWriter> writers = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            long totalLines = countLines(filePath);
            long currentLine = 0;
            String line;
            String header = null;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                currentLine++;
                if (currentLine % 1000 == 0 || currentLine == totalLines) {
                    printProgressBar(currentLine, totalLines, "Splitting Matches Data:");
                }
                if (isHeader) {
                    header = line;
                    isHeader = false;
                    continue;
                }
                String[] values = parseCsvLine(line);
                if (values.length < 1)
                    continue;
                if (values[10].equals("100") || values[10].equals("200")) {
                    for (int i = 18; i <= 27; i++) {
                        String champName = values[i];
                        if (!champions.contains(champName)) {
                            champions.add(champName);
                            saveChampion(champName);
                        }
                    }
                }

                String leagueStage = values[3] + '_' + values[4] + '_' + values[5]; // Assuming league stage info is in
                                                                                    // the first column
                if (!leagueStages.contains(leagueStage)) {
                    leagueStages.add(leagueStage);
                    writers.put(leagueStage,
                            new BufferedWriter(new FileWriter("data/matches_" + leagueStage + ".csv")));
                    writers.get(leagueStage).write(header);
                    writers.get(leagueStage).newLine();
                    paths.add("data/matches_" + leagueStage + ".csv");
                }
                BufferedWriter writer = writers.get(leagueStage);
                writer.write(line);
                writer.newLine();
            }
            for (BufferedWriter writer : writers.values()) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (String path : paths) {
            executor.execute(() -> processMatchData(path));
        }
        // Wait for all tasks to complete
        executor.shutdown();
        while (!executor.isTerminated())
            ;

        return paths;
    }

    public void processMatchData(String filePath) {
        String fileName = new File(filePath).getName();

        try {
            String currentHash = calculateFileHash(filePath);
            String configKey = "hash_" + fileName;
            Optional<AppConfig> configOpt = appConfigRepository.findById(configKey);
            if (configOpt.isPresent() && configOpt.get().getConfigValue().equals(currentHash)) {
                System.out.println("File " + fileName + " has not changed. Skipping import.");
                return;
            }
            AppConfig config = configOpt.orElse(AppConfig.builder().configKey(configKey).build());
            config.setConfigValue(currentHash);
            appConfigRepository.save(config);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            long totalLines = countLines(filePath);
            long currentLine = 0;
            String line;
            boolean isHeader = true;
            Map<String, List<String[]>> pendingGames = new HashMap<>();

            while ((line = br.readLine()) != null) {
                currentLine++;
                if (currentLine % 100 == 0 || currentLine == totalLines) {
                    printProgressBar(currentLine, totalLines, fileName + ":");
                }
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = parseCsvLine(line);
                if (values.length < 100)
                    continue; // Ensure enough columns
                if (values[3].equals("LCK") || values[3].equals("LPL")) {

                    // Save teams
                    try {
                        saveTeam(values[15]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String gameId = values[0];
                    pendingGames.computeIfAbsent(gameId, k -> new ArrayList<>()).add(values);

                    List<String[]> rows = pendingGames.get(gameId);
                    boolean hasTeam100 = rows.stream().anyMatch(r -> r[10].equals("100"));
                    boolean hasTeam200 = rows.stream().anyMatch(r -> r[10].equals("200"));

                    // Check if we have all rows (12 rows: 10 players + 2 teams)
                    if (hasTeam100 && hasTeam200 && rows.size() >= 12) {
                        processGameData(rows, filePath);
                        pendingGames.remove(gameId);
                    }
                }
            }
            // Process remaining games if any
            for (List<String[]> rows : pendingGames.values()) {
                boolean hasTeam100 = rows.stream().anyMatch(r -> r[10].equals("100"));
                boolean hasTeam200 = rows.stream().anyMatch(r -> r[10].equals("200"));
                if (hasTeam100 && hasTeam200) {
                    processGameData(rows, filePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processGameData(List<String[]> rows, String filePath) {
        String[] team100 = rows.stream().filter(r -> r[10].equals("100")).findFirst().orElse(null);
        String[] team200 = rows.stream().filter(r -> r[10].equals("200")).findFirst().orElse(null);

        if (team100 == null || team200 == null)
            return;

        List<String> teams = Arrays.asList(team100[15], team200[15]);
        try {
            saveMatch(team100, teams, filePath);

            List<String[]> playerRows = rows.stream()
                    .filter(r -> !r[10].equals("100") && !r[10].equals("200"))
                    .collect(Collectors.toList());

            gameService.createGameWithStats(team100, team200, playerRows);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("null")
    @Transactional
    private void saveChampion(String championName) {
        if (championName == null || championName.isEmpty())
            return;
        Champion champion = championRepository.findByName(championName).orElse(null);
        if (champion == null) {
            Champion newChampion = Champion.builder()
                    .name(championName)
                    .build();
            championRepository.save(newChampion);
        }
    }

    @Transactional
    private void saveMatch(String[] values, List<String> teams, String filePath) {
        if (values == null || values.length == 0)
            return;
        String teamAName = teams.get(0);
        String teamBName = teams.get(1);
        String stage = filePath.substring(filePath.indexOf("matches_") + 8, filePath.indexOf(".csv"));
        stage = stage.replace(' ', '_').replace('-', '_').toLowerCase();
        Stage matchStage = stageRepository.findByName(stage).orElse(null);
        Stage secondRead = null;
        if (matchStage == null) {
            // Create new stage
            synchronized (stageLock) {
                secondRead = stageRepository.findByName(stage).orElse(null);
                if (secondRead == null) {
                    matchStage = Stage.builder()
                            .name(stage)
                            .build();
                    stageRepository.save(matchStage);
                }
            }
        }

        // Ensure consistent ordering to allow games match lookup
        if (teamAName.compareTo(teamBName) > 0) {
            String temp = teamAName;
            teamAName = teamBName;
            teamBName = temp;
        }

        String date = values[7].split(" ")[0].trim();
        Team teamA = findOrCreateTeam(teamAName);
        Team teamB = findOrCreateTeam(teamBName);

        Optional<Match> existing = matchRepository.findByDateAndTeamAAndTeamB(date, teamA, teamB);
        if (existing.isPresent()) {
            return;
        }
        String timeISOString = values[7].trim().replace("/", "-").replace(" ", "T");
        LocalDateTime startTime = LocalDateTime.parse(timeISOString);

        League league = findOrCreateLeague(values[3]);

        Match newMatch = Match.builder()
                .matchId(values[0])
                .league(league)
                .teamA(teamA)
                .teamB(teamB)
                .startTime(startTime)
                .format(null)
                .winner(null)
                .teamAScore(0)
                .teamBScore(0)
                .stage(secondRead != null ? secondRead : matchStage)
                .date(date)
                .build();
        synchronized (matchLock) {
            // Double check inside lock
            if (matchRepository.findByDateAndTeamAAndTeamB(date, teamA, teamB).isPresent()) {
                return;
            }
            matchRepository.save(newMatch);
        }
    }

    @Transactional
    private League findOrCreateLeague(String leagueName) {
        return leagueRepository.findByName(leagueName)
                .orElseGet(() -> {
                    League newLeague = League.builder()
                            .name(leagueName)
                            .region("Unknown") // Default region
                            .build();
                    return leagueRepository.save(newLeague);
                });
    }

    @Transactional
    private void saveTeam(String teamName) {
        if (teamName == null || teamName.isEmpty())
            return;
        findOrCreateTeam(teamName);
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

    @SuppressWarnings("null")
    @Transactional
    private Team findOrCreateTeam(String teamName) {
        // Optimization: Check if exists before locking
        Optional<Team> existing = teamRepository.findByName(teamName);
        if (existing.isPresent()) {
            return existing.get();
        }

        synchronized (teamLock) {
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
    }

    private long countLines(String filePath) {
        try (java.util.stream.Stream<String> stream = java.nio.file.Files.lines(java.nio.file.Paths.get(filePath))) {
            return stream.count();
        } catch (IOException e) {
            return 0;
        }
    }

    private void printProgressBar(long current, long total, String message) {
        if (total == 0)
            return;
        int percent = (int) (current * 100 / total);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 50; i++) {
            if (i < (percent / 2)) {
                bar.append("=");
            } else {
                bar.append(" ");
            }
        }
        bar.append("] " + percent + "% (" + current + "/" + total + ")");
        System.out.print("\r" + message + " " + bar.toString());
        if (current == total) {
            System.out.println();
        }
    }

    private String calculateFileHash(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
