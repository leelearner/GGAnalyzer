package com.gganalyzer.api;

import com.gganalyzer.dto.StandingsDTO;
import com.gganalyzer.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/standings")
@CrossOrigin(origins = "http://localhost:5173")
public class StandingsController {

    @Autowired
    private StatsService statsService;

    @GetMapping
    public List<StandingsDTO> getStandings(@RequestParam(required = false) String league) {
        return statsService.getStandings(league);
    }
}
