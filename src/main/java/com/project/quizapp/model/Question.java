package com.project.quizapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private Long id;
    private String category;   // "ANIME_GAMING", "GENERAL"
    private String difficulty; // "EASY", "MEDIUM", "HARD"
    private String questionText;
    private List<String> options;
    private int correctIndex;
    private String explanation;
}
