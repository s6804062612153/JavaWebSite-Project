package com.project.quizapp.service;

import com.project.quizapp.dto.QuestionDTO;
import com.project.quizapp.dto.QuizRequest;
import com.project.quizapp.dto.QuizResultResponse;
import com.project.quizapp.model.Question;
import com.project.quizapp.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuizService service;

    private List<Question> sampleQuestions;

    @BeforeEach
    void setUp() {
        sampleQuestions = List.of(
            new Question(101L, "GENERAL", "EASY", "Question 1", List.of("A", "B", "C", "D"), 1, "Exp 1"),
            new Question(102L, "GENERAL", "EASY", "Question 2", List.of("A", "B", "C", "D"), 0, "Exp 2")
        );
    }

    private static QuizRequest requestWith(Map<Long, Integer> answers) {
        QuizRequest request = new QuizRequest();
        request.setUserAnswers(answers);
        return request;
    }

    @Test
    void shouldFilterQuestionsByCategoryAndDifficulty() {
        // sampleQuestions is an immutable list: getQuestions must still be able to shuffle it
        when(questionRepository.findByFilters(anyString(), anyString())).thenReturn(sampleQuestions);

        List<QuestionDTO> questions = service.getQuestions("GENERAL", "EASY", 10);
        assertEquals(2, questions.size());
    }

    @Test
    void shouldReturnAllAvailableQuestionsWhenRequestedLimitExceedsBankSize() {
        List<Question> many = IntStream.rangeClosed(1, 60)
                .mapToObj(i -> new Question((long) i, "GENERAL", "EASY", "Q" + i, List.of("A", "B", "C", "D"), 0, "E"))
                .toList();
        when(questionRepository.findByFilters(anyString(), anyString())).thenReturn(many);

        assertEquals(60, service.getQuestions("ALL", "ALL", 100).size());
        assertEquals(1, service.getQuestions("ALL", "ALL", -5).size());
        assertEquals(60, service.countQuestions("ALL", "ALL"));
    }

    @Test
    void shouldAcceptSubmissionForAllSixtyQuestionsInTheBank() {
        List<Question> many = IntStream.rangeClosed(1, 60)
                .mapToObj(i -> new Question((long) i, "GENERAL", "EASY", "Q" + i, List.of("A", "B", "C", "D"), 0, "E"))
                .toList();
        when(questionRepository.count()).thenReturn(60L);
        when(questionRepository.findAllById(anyIterable())).thenReturn(many);

        Map<Long, Integer> answers = new LinkedHashMap<>();
        for (long id = 1; id <= 60; id++) {
            answers.put(id, 0);
        }

        QuizResultResponse result = service.evaluateQuiz(requestWith(answers));
        assertEquals(60, result.getTotalQuestions());
        assertEquals(60, result.getCorrectCount());
    }

    @Test
    void shouldEvaluateCorrectAnswers() {
        when(questionRepository.count()).thenReturn(2L);
        when(questionRepository.findAllById(anyIterable())).thenReturn(List.of(sampleQuestions.get(0)));

        Map<Long, Integer> answers = new LinkedHashMap<>();
        answers.put(101L, 1); // correct answer (index 1)

        QuizResultResponse result = service.evaluateQuiz(requestWith(answers));
        assertEquals(1, result.getTotalQuestions());
        assertEquals(1, result.getCorrectCount());
    }

    @Test
    void shouldGiveGradeSAndFullScoreWhenEverythingIsCorrect() {
        when(questionRepository.count()).thenReturn(2L);
        when(questionRepository.findAllById(anyIterable())).thenReturn(sampleQuestions);

        Map<Long, Integer> answers = new LinkedHashMap<>();
        answers.put(101L, 1);
        answers.put(102L, 0);

        QuizResultResponse result = service.evaluateQuiz(requestWith(answers));
        assertEquals(2, result.getCorrectCount());
        assertEquals(200, result.getScore());   // 2 x EASY (100 points)
        assertEquals(100.0, result.getAccuracy());
        assertEquals("S", result.getGrade());
    }

    @Test
    void shouldTreatSkippedAndWrongAnswersAsIncorrectAndKeepSubmittedOrder() {
        when(questionRepository.count()).thenReturn(2L);
        when(questionRepository.findAllById(anyIterable())).thenReturn(sampleQuestions);

        Map<Long, Integer> answers = new LinkedHashMap<>();
        answers.put(102L, -1);  // skipped
        answers.put(101L, 3);   // wrong

        QuizResultResponse result = service.evaluateQuiz(requestWith(answers));
        assertEquals(0, result.getCorrectCount());
        assertEquals(0, result.getScore());
        assertEquals("D", result.getGrade());
        assertEquals(102L, result.getReviews().get(0).getId());
        assertEquals(101L, result.getReviews().get(1).getId());
    }

    @Test
    void shouldIgnoreUnknownQuestionIds() {
        when(questionRepository.count()).thenReturn(2L);
        when(questionRepository.findAllById(anyIterable())).thenReturn(List.of());

        Map<Long, Integer> answers = new LinkedHashMap<>();
        answers.put(999L, 0);

        QuizResultResponse result = service.evaluateQuiz(requestWith(answers));
        assertEquals(0, result.getTotalQuestions());
        assertEquals(0.0, result.getAccuracy());
    }

    @Test
    void shouldRejectRequestsWithMoreAnswersThanTheActualQuestionBank() {
        when(questionRepository.count()).thenReturn(50L);
        Map<Long, Integer> answers = new LinkedHashMap<>();
        for (long id = 1; id <= 51; id++) {
            answers.put(id, 0);
        }

        assertThrows(ResponseStatusException.class, () -> service.evaluateQuiz(requestWith(answers)));
    }
}
