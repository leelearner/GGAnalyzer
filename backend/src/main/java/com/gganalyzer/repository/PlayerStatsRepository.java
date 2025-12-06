package com.gganalyzer.repository;

import com.gganalyzer.model.Player;
import com.gganalyzer.model.Stage;
import com.gganalyzer.model.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {
    Optional<PlayerStats> findByPlayer(Player player);

    List<PlayerStats> findByStage(Stage stage);
}
