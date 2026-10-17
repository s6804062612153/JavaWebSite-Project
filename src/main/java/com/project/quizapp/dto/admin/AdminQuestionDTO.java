package com.project.quizapp.dto.admin;

import java.util.List;

/** Full question record for the password-protected administration API. */
public record AdminQuestionDTO(
        Long id,
        String category,
        String difficulty,
        String questionText,
        List<String> options,
        int correctIndex,
        String correctAnswer,
        String explanation
) {
}
