package com.gganalyzer.api;

import com.gganalyzer.dto.*;
import com.gganalyzer.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "http://localhost:5173")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/players")
    public List<PlayerStatsDTO> getPlayerStats(@RequestParam(required = true) String stage) {
        return statsService.getPlayerStats(stage);
    }

    @GetMapping("/teams")
    public List<TeamStatsDTO> getTeamStatsByStage(@RequestParam(required = false) String stage) {
        if (stage == null || stage.isEmpty()) {
            return statsService.getTeamStats();
        }
        return statsService.getTeamStatsByStage(stage);
    }

    @GetMapping("/champions")
    public List<ChampionStatsDTO> getChampionStats() {
        return statsService.getChampionStats();
    }

    @GetMapping("/stages")
    public List<StageDTO> getStages() {
        return statsService.getStages();
    }
}
