package com.project.quizapp.dto;

import lombok.Data;
import java.util.Map;

@Data
public class QuizRequest {
    private String category;
    private String difficulty;
    private Map<Long, Integer> userAnswers;
}
