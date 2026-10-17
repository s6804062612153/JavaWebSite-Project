package com.project.quizapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "questions",
        indexes = @Index(name = "idx_questions_category_difficulty", columnList = "category, difficulty"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String category;   // "ANIME_GAMING", "GENERAL"

    @Column(nullable = false, length = 16)
    private String difficulty; // "EASY", "MEDIUM", "HARD"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    // @OrderColumn keeps the options in the same order they were saved. Without it the
    // database may return them in any order, which would silently break correctIndex.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_order")
    @Column(name = "option_text", nullable = false, length = 500)
    private List<String> options;

    private int correctIndex;

    @Column(columnDefinition = "TEXT")
    private String explanation;
}
