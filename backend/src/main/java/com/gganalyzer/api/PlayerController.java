package com.gganalyzer.api;

import com.gganalyzer.dto.CreatePlayerRequest;
import com.gganalyzer.model.Player;
import com.gganalyzer.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "http://localhost:5173")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody CreatePlayerRequest request) {
        Player createdPlayer = playerService.createPlayer(request);
        return ResponseEntity.ok(createdPlayer);
    }

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }
}
