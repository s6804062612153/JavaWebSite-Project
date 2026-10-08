package com.project.quizapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class QuizResultResponse {
    private int totalQuestions;
    private int correctCount;
    private int score;
    private double accuracy;
    private String grade;
    private List<QuestionReviewDTO> reviews;
}
