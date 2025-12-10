package com.gganalyzer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gganalyzer.model.Champion;

@Repository
public interface ChampionRepository extends JpaRepository<Champion, Long> {
    Optional<Champion> findByName(String name);
}
