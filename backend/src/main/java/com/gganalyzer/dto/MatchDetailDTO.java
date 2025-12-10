package com.gganalyzer.dto;

import com.gganalyzer.model.Match;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MatchDetailDTO {
    private Match match;
    private List<GameDetailDTO> games;
}
