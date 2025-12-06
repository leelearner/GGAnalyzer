package com.gganalyzer.service;

import com.gganalyzer.model.AppConfig;
import com.gganalyzer.model.Player;
import com.gganalyzer.model.PlayerStats;
import com.gganalyzer.model.Team;
import com.gganalyzer.model.Stage;
import com.gganalyzer.repository.AppConfigRepository;
import com.gganalyzer.repository.PlayerRepository;
import com.gganalyzer.repository.PlayerStatsRepository;
import com.gganalyzer.repository.TeamRepository;
import com.gganalyzer.repository.StageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CsvImportService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerStatsRepository playerStatsRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AppConfigRepository appConfigRepository;

    @Autowired
    private StageRepository stageRepository;

    private static final String CSV_HASH_KEY = "csv_file_hash";

    @Transactional
    public void importPlayerStats(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return;

        String currentHash = calculateFileHash(file);
        String storedHash = appConfigRepository.findById(CSV_HASH_KEY)
                .map(AppConfig::getConfigValue)
                .orElse("");

        if (currentHash.equals(storedHash)) {
            System.out.println("CSV file has not changed. Skipping import.");
            return;
        }

        System.out.println("CSV file changed. Processing updates...");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = parseCsvLine(line);
                if (values.length < 29)
                    continue; // Ensure enough columns

                processRow(values);
            }

            // Update stored hash after successful processing
            AppConfig config = new AppConfig(CSV_HASH_KEY, currentHash);
            appConfigRepository.save(config);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String calculateFileHash(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
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

    private void processRow(String[] data) {
        // Columns:
        // "Player","Team","Pos","GP","W%","CTR%","K","D","A","KDA","KP","KS%","DTH%","FB%","GD10","XPD10","CSD10","CSPM","CS%P15","DPM","DMG%","D%P15","TDPG","EGPM","GOLD%","STL","WPM","CWPM","WCPM"
        // Indices: 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25
        // 26 27 28

        String playerName = data[0];
        String teamName = data[1];
        String position = data[2];

        // Find or create Team
        Team team = teamRepository.findByName(teamName)
                .orElseGet(() -> {
                    Team newTeam = Team.builder()
                            .name(teamName)
                            .acronym(teamName.length() > 3 ? teamName.substring(0, 3).toUpperCase()
                                    : teamName.toUpperCase())
                            .build();
                    return teamRepository.save(newTeam);
                });

        // Find or create Player
        Player player = playerRepository.findByHandle(playerName)
                .orElseGet(() -> {
                    Player newPlayer = Player.builder()
                            .handle(playerName)
                            .role(position)
                            .team(team)
                            .build();
                    return playerRepository.save(newPlayer);
                });

        // Update player team/role if changed
        if (player.getTeam() == null || !player.getTeam().getId().equals(team.getId())) {
            player.setTeam(team);
        }
        player.setRole(position);
        playerRepository.save(player);

        // Create or Update PlayerStats
        PlayerStats stats = playerStatsRepository.findByPlayer(player)
                .orElse(new PlayerStats());

        // Check if update is needed (simple check on games played and last updated, or
        // check all fields)
        // For "only differences", we should check if values changed.
        // Comparing a few key metrics or all. Let's compare all relevant ones to be
        // safe.

        int newGamesPlayed = parseInt(data[3]);
        double newWinRate = parsePercentage(data[4]);
        double newKda = parseDouble(data[9]);
        double newDpm = parseDouble(data[19]);

        boolean isNew = stats.getId() == null;
        boolean changed = isNew ||
                stats.getGamesPlayed() != newGamesPlayed ||
                Math.abs(stats.getWinRate() - newWinRate) > 0.001 ||
                Math.abs(stats.getKda() - newKda) > 0.001 ||
                Math.abs(stats.getDpm() - newDpm) > 0.001;

        // TODO: Set stage for player stats according to the csv file name
        Stage defaultStage = stageRepository.findAll().get(0);

        // If not changed based on key metrics, we might skip.
        // But to be fully correct per user request "only differences", we should
        // probably update if ANY field changed.
        // Given the number of fields, let's just update if key metrics change or if
        // it's new.
        // Or better, let's just set the values. Hibernate will only issue an UPDATE if
        // the state actually changed.
        // However, setting LastUpdated will force an update.
        // So we should only set LastUpdated if we actually change something.

        if (!changed) {
            // Check a few more to be sure? Or rely on Hibernate dirty checking?
            // Hibernate dirty checking works if we load the entity and modify it.
            // But we need to avoid setting LastUpdated if nothing else changed.
            // Let's set all fields, and check if Hibernate detects changes.
            // But we can't easily know if Hibernate detected changes before saving.
            // So let's do the manual check for "changed" flag properly.

            // Actually, simpler approach: Set all fields. If they are same, Hibernate won't
            // update.
            // EXCEPT LastUpdated. So we only set LastUpdated if we detect a change.
        }

        stats.setPlayer(player);
        stats.setGamesPlayed(newGamesPlayed);
        stats.setWinRate(newWinRate);
        stats.setCounterPickRate(parsePercentage(data[5]));
        stats.setKills(parseInt(data[6]));
        stats.setDeaths(parseInt(data[7]));
        stats.setAssists(parseInt(data[8]));
        stats.setKda(newKda);
        stats.setKillParticipation(parsePercentage(data[10]));
        stats.setKillShare(parsePercentage(data[11]));
        stats.setDeathShare(parsePercentage(data[12]));
        stats.setFirstBloodRate(parsePercentage(data[13]));
        stats.setGoldDiff10(parseInt(data[14]));
        stats.setXpDiff10(parseInt(data[15]));
        stats.setCsDiff10(parseDouble(data[16]));
        stats.setCspm(parseDouble(data[17]));
        stats.setCsSharePost15(parsePercentage(data[18]));
        stats.setDpm(newDpm);
        stats.setDamageShare(parsePercentage(data[20]));
        stats.setDamageSharePost15(parsePercentage(data[21]));
        stats.setTotalDamagePerGame(parseInt(data[22]));
        stats.setEarnedGoldPerMinute(parseDouble(data[23]));
        stats.setGoldShare(parsePercentage(data[24]));
        stats.setSteals(parseInt(data[25]));
        stats.setWardsPerMinute(parseDouble(data[26]));
        stats.setControlWardsPerMinute(parseDouble(data[27]));
        stats.setWardsClearedPerMinute(parseDouble(data[28]));
        stats.setStage(defaultStage);

        // Save. Hibernate will only execute UPDATE if fields changed.
        // @PreUpdate and @PrePersist in PlayerStats will handle lastUpdated.
        playerStatsRepository.save(stats);
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double parsePercentage(String value) {
        String clean = value.replace("%", "").trim();
        return parseDouble(clean);
    }

    public List<String> splitMatchesData(String filePath) {
        List<String> leagueStages = new ArrayList<>();
        Map<String, BufferedWriter> writers = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = parseCsvLine(line);
                if (values.length < 1)
                    continue;

                String leagueStage = values[3] + '_' + values[4] + '_' + values[5]; // Assuming league stage info is in
                                                                                    // the first column
                if (!leagueStages.contains(leagueStage)) {
                    leagueStages.add(leagueStage);
                    writers.put(leagueStage,
                            new BufferedWriter(new FileWriter("data/matches_" + leagueStage + ".csv")));
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
        return leagueStages;
    }
}
