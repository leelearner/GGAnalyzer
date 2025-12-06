package com.gganalyzer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "players")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String handle; // e.g., Faker

    private String role; // Top, Jungle, Mid, Bot, Support

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private String photoUrl;
}
