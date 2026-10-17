package com.project.quizapp.service;

import com.project.quizapp.dto.QuestionDTO;
import com.project.quizapp.dto.QuestionReviewDTO;
import com.project.quizapp.dto.QuizRequest;
import com.project.quizapp.dto.QuizResultResponse;
import com.project.quizapp.model.Question;
import com.project.quizapp.repository.QuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final int MIN_LIMIT = 1;

    private final QuestionRepository questionRepository;

    public QuizService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<QuestionDTO> getQuestions(String category, String difficulty, int limit) {
        // Copy first: the repository result may be unmodifiable, and shuffle should work.
        List<Question> filtered = new ArrayList<>(filterQuestions(category, difficulty));
        if (filtered.isEmpty()) {
            return List.of();
        }
        // The available filtered question count is the only maximum; there is no fixed 20-question cap.
        int safeLimit = Math.max(MIN_LIMIT, Math.min(limit, filtered.size()));
        Collections.shuffle(filtered);
        return filtered.stream()
                .limit(safeLimit)
                .map(q -> new QuestionDTO(q.getId(), q.getCategory(), q.getDifficulty(), q.getQuestionText(), q.getOptions()))
                .collect(Collectors.toList());
    }

    public int countQuestions(String category, String difficulty) {
        return filterQuestions(category, difficulty).size();
    }

    private List<Question> filterQuestions(String category, String difficulty) {
        return questionRepository.findByFilters(
                category != null ? category.trim() : "ALL",
                difficulty != null ? difficulty.trim() : "ALL"
        );
    }

    public QuizResultResponse evaluateQuiz(QuizRequest request) {
        Map<Long, Integer> userAnswers = request != null && request.getUserAnswers() != null
                ? request.getUserAnswers()
                : new LinkedHashMap<>();

        long questionBankSize = questionRepository.count();
        if (userAnswers.size() > questionBankSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Too many answers in one request (question bank contains " + questionBankSize + " questions)");
        }

        // one query for all submitted ids instead of one query per answer
        Map<Long, Question> questionsById = questionRepository.findAllById(userAnswers.keySet()).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<QuestionReviewDTO> reviews = new ArrayList<>();
        int correctCount = 0;
        int totalScore = 0;

        for (Map.Entry<Long, Integer> entry : userAnswers.entrySet()) {
            Question q = questionsById.get(entry.getKey());
            if (q == null) {
                continue;
            }

            Integer userChoice = entry.getValue();
            boolean isCorrect = userChoice != null && userChoice >= 0 && userChoice == q.getCorrectIndex();

            if (isCorrect) {
                correctCount++;
                totalScore += pointsFor(q.getDifficulty());
            }

            reviews.add(new QuestionReviewDTO(
                    q.getId(),
                    q.getQuestionText(),
                    q.getOptions(),
                    userChoice == null ? -1 : userChoice,
                    q.getCorrectIndex(),
                    isCorrect,
                    q.getExplanation()
            ));
        }

        int totalQuestions = reviews.size();
        double accuracy = totalQuestions == 0 ? 0.0 : (correctCount * 100.0) / totalQuestions;
        String grade = gradeFor(accuracy);

        return new QuizResultResponse(totalQuestions, correctCount, totalScore, accuracy, grade, reviews);
    }

    private int pointsFor(String difficulty) {
        if (difficulty == null) return 100;
        return switch (difficulty.trim().toUpperCase()) {
            case "HARD" -> 200;
            case "MEDIUM" -> 150;
            default -> 100;
        };
    }

    private String gradeFor(double accuracy) {
        if (accuracy >= 90) return "S";
        if (accuracy >= 80) return "A";
        if (accuracy >= 70) return "B";
        if (accuracy >= 60) return "C";
        return "D";
    }
}