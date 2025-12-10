package com.gganalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gganalyzer.model.Ban;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {
}
