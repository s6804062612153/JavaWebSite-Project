package com.project.quizapp.dto.admin;

import java.util.Map;

/** Aggregate view of the question bank for the administration dashboard. */
public record AdminQuestionSummaryDTO(
        long totalQuestions,
        Map<String, Long> categories,
        Map<String, Long> difficulties,
        Map<String, Map<String, Long>> categoryDifficulty
) {
}
