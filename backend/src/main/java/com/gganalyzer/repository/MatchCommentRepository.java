package com.gganalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gganalyzer.model.Match;
import com.gganalyzer.model.MatchComment;

@Repository
public interface MatchCommentRepository extends JpaRepository<MatchComment, Long> {
    public List<MatchComment> findByMatch(Match match);
}
