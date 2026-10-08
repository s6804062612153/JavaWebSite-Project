package com.project.quizapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class QuestionReviewDTO {
    private Long id;
    private String questionText;
    private List<String> options;
    private int userSelected;
    private int correctIndex;
    private boolean isCorrect;
    private String explanation;
}
