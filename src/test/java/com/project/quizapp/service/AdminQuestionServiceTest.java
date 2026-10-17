package com.project.quizapp.service;

import com.project.quizapp.dto.admin.AdminQuestionDTO;
import com.project.quizapp.dto.admin.AdminQuestionSummaryDTO;
import com.project.quizapp.model.Question;
import com.project.quizapp.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminQuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Test
    void shouldSummarizeQuestionCountsByCategoryAndDifficulty() {
        when(questionRepository.count()).thenReturn(60L);
        when(questionRepository.countQuestionsGroupedByCategory()).thenReturn(List.of(
                new Object[]{"ANIME_GAMING", 30L}, new Object[]{"GENERAL", 30L}));
        when(questionRepository.countQuestionsGroupedByDifficulty()).thenReturn(List.of(
                new Object[]{"EASY", 20L}, new Object[]{"MEDIUM", 20L}, new Object[]{"HARD", 20L}));
        when(questionRepository.countQuestionsGroupedByCategoryAndDifficulty()).thenReturn(List.of(
                new Object[]{"ANIME_GAMING", "EASY", 10L}, new Object[]{"ANIME_GAMING", "MEDIUM", 10L},
                new Object[]{"ANIME_GAMING", "HARD", 10L}, new Object[]{"GENERAL", "EASY", 10L},
                new Object[]{"GENERAL", "MEDIUM", 10L}, new Object[]{"GENERAL", "HARD", 10L}));

        AdminQuestionSummaryDTO summary = new AdminQuestionService(questionRepository).getSummary();

        assertEquals(60L, summary.totalQuestions());
        assertEquals(30L, summary.categories().get("ANIME_GAMING"));
        assertEquals(30L, summary.categories().get("GENERAL"));
        assertEquals(20L, summary.difficulties().get("EASY"));
        assertEquals(10L, summary.categoryDifficulty().get("GENERAL").get("HARD"));
    }

    @Test
    void shouldReturnFullAdminDetailsIncludingCorrectAnswer() {
        Question question = new Question(7L, "GENERAL", "EASY", "เมืองหลวงของญี่ปุ่นคือเมืองใด?",
                List.of("โอซาก้า", "เกียวโต", "โตเกียว", "นาโกย่า"), 2, "เมืองหลวงของญี่ปุ่นคือโตเกียว");
        when(questionRepository.searchForAdmin(eq("GENERAL"), eq("EASY"), eq("ญี่ปุ่น"), any()))
                .thenReturn(new PageImpl<>(List.of(question), PageRequest.of(0, 20, Sort.by("id")), 1));

        var result = new AdminQuestionService(questionRepository).search(
                " general ", " easy ", " ญี่ปุ่น ", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        AdminQuestionDTO dto = result.getContent().get(0);
        assertEquals(7L, dto.id());
        assertEquals("โตเกียว", dto.correctAnswer());
        assertEquals(4, dto.options().size());
        assertEquals("เมืองหลวงของญี่ปุ่นคือโตเกียว", dto.explanation());
    }
}
