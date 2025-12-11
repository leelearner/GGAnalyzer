package com.gganalyzer.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.gganalyzer.service.CommentService;
import com.gganalyzer.service.MatchService;

import com.gganalyzer.model.Match;
import com.gganalyzer.model.MatchComment;
import com.gganalyzer.dto.MatchCommentDTO;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:5173")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private MatchService matchService;

    @GetMapping("/match")
    public List<MatchCommentDTO> getMatchComment(@RequestParam(required = true) String matchId) {
        Match match = matchService.findByMathId(matchId);
        if (match == null) {
            log.warn("Match not found for matchId: {}", matchId);
            return null;
        }
        List<MatchComment> comments = commentService.findMatchCommentByMatch(match);
        return comments.stream()
                .map(c -> MatchCommentDTO.builder()
                        .comment(c.getComment())
                        .rating(c.getRating())
                        .build())
                .toList();
    }

    @PostMapping("/match")
    public void postMatchComment(@RequestParam(required = true) String matchId,
            @RequestBody MatchCommentDTO commentDTO) {
        Match match = matchService.findByMathId(matchId);
        if (match == null) {
            log.warn("Match not found for matchId: {}", matchId);
            return;
        }
        MatchComment comment = new MatchComment();
        comment.setMatch(match);
        comment.setComment(commentDTO.getComment());
        comment.setRating(commentDTO.getRating());
        commentService.saveMatchComment(comment);
    }
}
