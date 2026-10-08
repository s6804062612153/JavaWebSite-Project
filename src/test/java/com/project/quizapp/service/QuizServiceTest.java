package com.project.quizapp.service;

import com.project.quizapp.dto.QuestionDTO;
import com.project.quizapp.dto.QuizRequest;
import com.project.quizapp.dto.QuizResultResponse;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QuizServiceTest {

    private final QuizService service = new QuizService();

    @Test
    void shouldFilterQuestionsByCategoryAndDifficulty() {
        List<QuestionDTO> questions = service.getQuestions("GENERAL", "EASY", 10);

        assertFalse(questions.isEmpty());
        assertTrue(questions.size() <= 10);
        assertTrue(questions.stream().allMatch(q -> "GENERAL".equals(q.getCategory())));
        assertTrue(questions.stream().allMatch(q -> "EASY".equals(q.getDifficulty())));
    }

    @Test
    void shouldClampRequestedLimit() {
        List<QuestionDTO> questions = service.getQuestions("ALL", "ALL", 999);

        assertEquals(20, questions.size());
    }

    @Test
    void shouldEvaluateCorrectIncorrectAndSkippedAnswers() {
        QuizRequest request = new QuizRequest();
        Map<Long, Integer> answers = new LinkedHashMap<>();
        answers.put(101L, 1); // correct, EASY = 100
        answers.put(102L, 0); // correct, EASY = 100
        answers.put(103L, -1); // skipped
        request.setUserAnswers(answers);

        QuizResultResponse result = service.evaluateQuiz(request);

        assertEquals(3, result.getTotalQuestions());
        assertEquals(2, result.getCorrectCount());
        assertEquals(200, result.getScore());
        assertEquals(66.6666666667, result.getAccuracy(), 0.0000001);
        assertEquals("C", result.getGrade());
        assertEquals(3, result.getReviews().size());
    }

    @Test
    void shouldCountQuestionsMatchingFilters() {
        int all = service.countQuestions("ALL", "ALL");
        int general = service.countQuestions("GENERAL", "ALL");
        int animeGaming = service.countQuestions("ANIME_GAMING", "ALL");

        assertTrue(all > 0);
        assertEquals(all, general + animeGaming);
        assertEquals(service.getQuestions("GENERAL", "HARD", 20).size(),
                Math.min(service.countQuestions("GENERAL", "HARD"), 20));
    }

    @Test
    void maxLimitShouldNeverExceedServiceCap() {
        assertTrue(service.maxLimitFor("ALL", "ALL") <= 20);
        assertEquals(0, service.maxLimitFor("UNKNOWN", "ALL"));
    }
}
