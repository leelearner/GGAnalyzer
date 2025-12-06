package com.gganalyzer.repository;

import com.gganalyzer.model.Champion;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChampionRepository extends JpaRepository<Champion, Long> {
    Optional<Champion> findByName(String name);
}
