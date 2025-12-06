package com.gganalyzer.config;

import com.gganalyzer.model.League;
import com.gganalyzer.model.Match;
import com.gganalyzer.model.Team;
import com.gganalyzer.repository.LeagueRepository;
import com.gganalyzer.repository.MatchRepository;
import com.gganalyzer.repository.TeamRepository;
import com.gganalyzer.service.CsvImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private CsvImportService csvImportService;

    @Override
    public void run(String... args) throws Exception {
        if (leagueRepository.count() == 0) {
            seedData();
        }
        // Import CSV data
        csvImportService.importPlayerStats(
                "/home/persona/Documents/GGAnalyzer/backend/LCK 2025 Rounds 1-2 - Player Stats - OraclesElixir.csv");
        csvImportService.splitMatchesData("data/2025_LoL_esports_match_data_from_OraclesElixir.csv");
        System.out.println("Splitting completed.");
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

        Match m1 = Match.builder()
                .league(lck)
                .teamA(t1)
                .teamB(gen)
                .startTime(LocalDateTime.now().plusHours(2))
                .format("bo3")
                .teamAScore(0)
                .teamBScore(0)
                .build();

        Match m2 = Match.builder()
                .league(lpl)
                .teamA(blg)
                .teamB(jdg)
                .startTime(LocalDateTime.now().minusDays(1))
                .format("bo3")
                .winner(blg)
                .teamAScore(2)
                .teamBScore(1)
                .build();

        matchRepository.saveAll(Arrays.asList(m1, m2));

        System.out.println("Data seeded successfully!");
    }
}
