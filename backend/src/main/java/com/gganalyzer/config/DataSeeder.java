package com.gganalyzer.config;

import com.gganalyzer.model.Champion;
import com.gganalyzer.model.League;
import com.gganalyzer.model.Player;
import com.gganalyzer.model.Stage;
import com.gganalyzer.model.Team;
import com.gganalyzer.repository.ChampionRepository;
import com.gganalyzer.repository.LeagueRepository;
import com.gganalyzer.repository.PlayerRepository;
import com.gganalyzer.repository.StageRepository;
import com.gganalyzer.repository.TeamRepository;
import com.gganalyzer.service.CsvImportService;
import com.gganalyzer.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private LeagueRepository leagueRepository;
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

    @Override
    public void run(String... args) throws Exception {
        if (leagueRepository.count() == 0) {
            seedData();
        }
        csvImportService.splitMatchesData("data/2025_LoL_esports_match_data_from_OraclesElixir.csv");
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
        while (!executor.isTerminated())
            ;
        System.out.println("Player stats calculation completed.");
        System.out.println("Team stats calculation completed.");

        System.out.println("Assigning images to players, teams, and champions...");
        File imagesDir = new File("src/main/resources/static/images");
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
                    Team team = teamRepository.findByName(teamName).orElse(null);
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
            System.out.println("Images assigned successfully.");
        }
    }

    private void seedData() {
        League lck = League.builder().name("LCK").region("South Korea").build();
        League lpl = League.builder().name("LPL").region("China").build();
        leagueRepository.saveAll(Arrays.asList(lck, lpl));

        Team t1 = Team.builder().name("T1").acronym("T1").build();
        Team gen = Team.builder().name("Gen.G").acronym("GEN").build();
        Team blg = Team.builder().name("Bilibili Gaming").acronym("BLG").build();
        Team jdg = Team.builder().name("JD Gaming").acronym("JDG").build();
        teamRepository.saveAll(Arrays.asList(t1, gen, blg, jdg));

        System.out.println("Data seeded successfully!");
    }
}
