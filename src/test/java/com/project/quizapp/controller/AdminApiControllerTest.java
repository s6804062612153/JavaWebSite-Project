package com.project.quizapp.controller;

import com.project.quizapp.dto.admin.AdminQuestionDTO;
import com.project.quizapp.dto.admin.AdminQuestionSummaryDTO;
import com.project.quizapp.service.AdminQuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminApiControllerTest {

    @Test
    void shouldReturnSummaryWithoutPassword() {
        AdminQuestionService service = mock(AdminQuestionService.class);
        AdminQuestionSummaryDTO summary = new AdminQuestionSummaryDTO(0L, Map.of(), Map.of(), Map.of());
        when(service.getSummary()).thenReturn(summary);
        AdminApiController controller = new AdminApiController(service);

        assertSame(summary, controller.summary());
    }

    @Test
    void shouldReturnQuestionsWithoutPassword() {
        AdminQuestionService service = mock(AdminQuestionService.class);
        when(service.search(eq("ALL"), eq("ALL"), eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<AdminQuestionDTO>(List.of()));
        AdminApiController controller = new AdminApiController(service);

        var result = controller.questions("ALL", "ALL", "", 0, 20);

        assertTrue(result.getContent().isEmpty());
        assertEquals(20, result.getSize());
    }

    @Test
    void shouldRejectNegativePageNumber() {
        AdminApiController controller = new AdminApiController(mock(AdminQuestionService.class));

        var exception = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> controller.questions("ALL", "ALL", "", -1, 20));

        assertEquals(400, exception.getStatusCode().value());
    }
}
