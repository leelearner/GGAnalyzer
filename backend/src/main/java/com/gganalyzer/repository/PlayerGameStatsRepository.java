package com.gganalyzer.repository;

import com.gganalyzer.model.Player;
import com.gganalyzer.model.PlayerGameStats;
import com.gganalyzer.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlayerGameStatsRepository extends JpaRepository<PlayerGameStats, Long> {
    List<PlayerGameStats> findByGame(Game game);

    boolean existsByGameAndPlayer(Game game, Player player);
}
