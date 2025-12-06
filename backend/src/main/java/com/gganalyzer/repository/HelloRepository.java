package com.gganalyzer.repository;

import java.util.List;
import com.gganalyzer.model.Hello;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface HelloRepository extends JpaRepository<Hello, Long> {
    List<Hello> findByContent(String content);
}
