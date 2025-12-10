package com.gganalyzer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchCommentDTO {
    private String comment;
    private Double rating;
}
