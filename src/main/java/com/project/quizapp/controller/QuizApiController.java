package com.project.quizapp.controller;

import com.project.quizapp.dto.QuizRequest;
import com.project.quizapp.dto.QuizResultResponse;
import com.project.quizapp.dto.QuestionDTO;
import com.project.quizapp.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
public class QuizApiController {

    private final QuizService quizService;

    public QuizApiController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "quiz-api",
                "version", com.project.quizapp.QuizApplication.APP_VERSION
        ));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> countQuestions(
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "ALL") String difficulty) {

        return ResponseEntity.ok(Map.of(
                "available", quizService.countQuestions(category, difficulty),
                "maxLimit", quizService.maxLimitFor(category, difficulty)
        ));
    }

    @GetMapping("/questions")
    public ResponseEntity<List<QuestionDTO>> getQuestions(
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "ALL") String difficulty,
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(quizService.getQuestions(category, difficulty, limit));
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizResultResponse> submitAnswers(@RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.evaluateQuiz(request));
    }
}
