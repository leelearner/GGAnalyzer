package com.gganalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gganalyzer.model.AppConfig;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
}
