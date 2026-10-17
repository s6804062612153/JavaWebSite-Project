package com.project.quizapp.controller;

import com.project.quizapp.dto.admin.AdminQuestionDTO;
import com.project.quizapp.dto.admin.AdminQuestionSummaryDTO;
import com.project.quizapp.service.AdminQuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final AdminQuestionService adminQuestionService;

    public AdminApiController(AdminQuestionService adminQuestionService) {
        this.adminQuestionService = adminQuestionService;
    }

    @GetMapping("/summary")
    public AdminQuestionSummaryDTO summary() {
        return adminQuestionService.getSummary();
    }

    @GetMapping("/questions")
    public Page<AdminQuestionDTO> questions(
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "ALL") String difficulty,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be zero or greater");
        }
        int safeSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.ASC, "id"));
        return adminQuestionService.search(category, difficulty, keyword, pageable);
    }
}
