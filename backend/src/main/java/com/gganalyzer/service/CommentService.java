package com.gganalyzer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gganalyzer.model.MatchComment;
import com.gganalyzer.repository.MatchCommentRepository;
import com.gganalyzer.model.Match;

@Service
public class CommentService {
    @Autowired
    private MatchCommentRepository matchCommentRepository;

    public List<MatchComment> findMatchCommentByMatch(Match match) {
        return matchCommentRepository.findByMatch(match);
    }

    public MatchComment saveMatchComment(MatchComment comment) {
        return matchCommentRepository.save(comment);
    }
}
