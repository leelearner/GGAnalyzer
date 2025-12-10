package com.gganalyzer.config;

import com.gganalyzer.model.League;
import com.gganalyzer.model.Stage;
import com.gganalyzer.model.Team;
import com.gganalyzer.repository.LeagueRepository;
import com.gganalyzer.repository.MatchRepository;
import com.gganalyzer.repository.StageRepository;
import com.gganalyzer.repository.TeamRepository;
import com.gganalyzer.service.CsvImportService;
import com.gganalyzer.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
