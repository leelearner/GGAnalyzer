package com.gganalyzer.dto;

import lombok.Data;

@Data
public class CreatePlayerRequest {
    private String handle;
    private String role;
    private Long teamId;
    private String photoUrl;
}
