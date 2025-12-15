package com.gganalyzer.config;

import com.gganalyzer.model.Champion;
import com.gganalyzer.model.Player;
import com.gganalyzer.model.Stage;
import com.gganalyzer.model.Team;
import com.gganalyzer.repository.ChampionRepository;
import com.gganalyzer.repository.PlayerRepository;
import com.gganalyzer.repository.StageRepository;
import com.gganalyzer.repository.TeamRepository;
import com.gganalyzer.service.CsvImportService;
import com.gganalyzer.service.GoogleDriveService;
import com.gganalyzer.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private CsvImportService csvImportService;
    @Autowired
    private StatsService statsService;
    @Autowired
    private StageRepository stageRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private ChampionRepository championRepository;
    @Autowired
    private GoogleDriveService googleDriveService;

    @Override
    public void run(String... args) throws Exception {
        // scheduledDownloadAndProcess();
        processData();
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000, initialDelay = 60 * 1000)
    public void scheduledDownloadAndProcess() {
        try {
            System.out.println("Starting scheduled task: Download and process data...");
            String folderId = "1gLSw0RLjBbtaNy0dgnGQDAZOHIgCe-HH";
            String fileName = "2025_LoL_esports_match_data_from_OraclesElixir.csv";
            String destinationPath = "data/resources/" + fileName;

            try {
                googleDriveService.downloadFile(folderId, fileName, destinationPath);
            } catch (Exception e) {
                System.err.println("Failed to download file from Google Drive: " + e.getMessage());
            }
            processData();
            System.out.println("Data update completed successfully.");
        } catch (Exception e) {
            System.err.println("Error in updating data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void processData() {
        File dataDir = new File("data/resources");
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            System.err.println("Data directory does not exist: " + dataDir.getAbsolutePath());
            return;
        }
        for (File dataFile : dataDir.listFiles()) {
            csvImportService.splitMatchesData(dataFile.getAbsolutePath());
        }
        // csvImportService.processMatchData("data/matches_LCK_2025_Rounds 1-2.csv");
        System.out.println("Import completed. Calculating player stats...");
        System.out.println("Calculating team stats...");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Stage> stages = stageRepository.findAll();
        for (Stage stage : stages) {
            executor.execute(() -> statsService.calculatePlayerStats(stage));
            executor.execute(() -> statsService.calculateTeamStats(stage));
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Player stats calculation completed.");
        System.out.println("Team stats calculation completed.");

        System.out.println("Assigning images to players, teams, and champions...");
        File imagesDir = new File("src/main/resources/static/images");
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            for (File dirs : imagesDir.listFiles()) {
                if (dirs.isDirectory() && dirs.getName().contains("players")) {
                    for (File file : dirs.listFiles()) {
                        String playerName = file.getName().split("\\.")[0];
                        Player player = playerRepository.findByHandle(playerName).orElse(null);
                        if (player == null) {
                            continue;
                        }
                        if (player.getPhotoUrl() == null) {
                            player.setPhotoUrl("http://localhost:8080/images/players/" + file.getName());
                            playerRepository.save(player);
                        }
                    }
                } else if (dirs.isDirectory() && dirs.getName().contains("teams")) {
                    for (File file : dirs.listFiles()) {
                        String teamName = file.getName().split("\\.")[0];
                        Team team = teamRepository.findByAcronym(teamName).orElse(null);
                        if (team == null) {
                            continue;
                        }
                        if (team.getLogoUrl() == null) {
                            team.setLogoUrl("http://localhost:8080/images/teams/" + file.getName());
                            teamRepository.save(team);
                        }
                    }
                } else if (dirs.isDirectory() && dirs.getName().contains("champions")) {
                    for (File file : dirs.listFiles()) {
                        String championName = file.getName().split("\\.")[0];
                        Champion champion = championRepository.findByName(championName).orElse(null);
                        if (champion == null) {
                            continue;
                        }
                        if (champion.getImageUrl() == null) {
                            champion.setImageUrl("http://localhost:8080/images/champions/" + file.getName());
                            championRepository.save(champion);
                        }
                    }
                }
            }
            System.out.println("Images assigned successfully.");
        }
    }
}
