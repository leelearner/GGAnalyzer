package com.gganalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gganalyzer.model.Stage;

import java.util.Optional;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
    Optional<Stage> findByName(String name);
}
