package com.gganalyzer.repository;

import com.gganalyzer.model.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {
}
