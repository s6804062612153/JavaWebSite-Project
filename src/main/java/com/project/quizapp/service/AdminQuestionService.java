package com.project.quizapp.service;

import com.project.quizapp.dto.admin.AdminQuestionDTO;
import com.project.quizapp.dto.admin.AdminQuestionSummaryDTO;
import com.project.quizapp.model.Question;
import com.project.quizapp.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminQuestionService {

    private static final List<String> CATEGORIES = List.of("ANIME_GAMING", "GENERAL");
    private static final List<String> DIFFICULTIES = List.of("EASY", "MEDIUM", "HARD");

    private final QuestionRepository questionRepository;

    public AdminQuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public AdminQuestionSummaryDTO getSummary() {
        Map<String, Long> categories = new LinkedHashMap<>();
        Map<String, Long> difficulties = new LinkedHashMap<>();
        Map<String, Map<String, Long>> matrix = new LinkedHashMap<>();

        CATEGORIES.forEach(category -> {
            categories.put(category, 0L);
            Map<String, Long> row = new LinkedHashMap<>();
            DIFFICULTIES.forEach(difficulty -> row.put(difficulty, 0L));
            matrix.put(category, row);
        });
        DIFFICULTIES.forEach(difficulty -> difficulties.put(difficulty, 0L));

        // Use grouped SQL queries rather than issuing one COUNT query for every cell.
        for (Object[] result : questionRepository.countQuestionsGroupedByCategory()) {
            categories.put((String) result[0], ((Number) result[1]).longValue());
        }
        for (Object[] result : questionRepository.countQuestionsGroupedByDifficulty()) {
            difficulties.put((String) result[0], ((Number) result[1]).longValue());
        }
        for (Object[] result : questionRepository.countQuestionsGroupedByCategoryAndDifficulty()) {
            String category = (String) result[0];
            String difficulty = (String) result[1];
            Map<String, Long> row = matrix.computeIfAbsent(category, key -> new LinkedHashMap<>());
            DIFFICULTIES.forEach(level -> row.putIfAbsent(level, 0L));
            row.put(difficulty, ((Number) result[2]).longValue());
        }

        return new AdminQuestionSummaryDTO(questionRepository.count(), categories, difficulties, matrix);
    }

    public Page<AdminQuestionDTO> search(String category, String difficulty, String keyword, Pageable pageable) {
        String safeCategory = normalizeFilter(category);
        String safeDifficulty = normalizeFilter(difficulty);
        String safeKeyword = keyword == null ? "" : keyword.trim();

        return questionRepository.searchForAdmin(safeCategory, safeDifficulty, safeKeyword, pageable)
                .map(this::toAdminDTO);
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? "ALL" : value.trim().toUpperCase();
    }

    private AdminQuestionDTO toAdminDTO(Question question) {
        List<String> options = question.getOptions() == null ? List.of() : List.copyOf(question.getOptions());
        int correctIndex = question.getCorrectIndex();
        String correctAnswer = correctIndex >= 0 && correctIndex < options.size()
                ? options.get(correctIndex) : "";
        return new AdminQuestionDTO(
                question.getId(), question.getCategory(), question.getDifficulty(),
                question.getQuestionText(), options, correctIndex, correctAnswer, question.getExplanation());
    }
}
