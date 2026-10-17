package com.project.quizapp.repository;

import com.project.quizapp.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE " +
           "(:category = 'ALL' OR UPPER(q.category) = UPPER(:category)) AND " +
           "(:difficulty = 'ALL' OR UPPER(q.difficulty) = UPPER(:difficulty))")
    List<Question> findByFilters(@Param("category") String category,
                                 @Param("difficulty") String difficulty);

    @Query("SELECT q FROM Question q WHERE " +
           "(:category = 'ALL' OR UPPER(q.category) = UPPER(:category)) AND " +
           "(:difficulty = 'ALL' OR UPPER(q.difficulty) = UPPER(:difficulty)) AND " +
           "(:keyword = '' OR LOWER(q.questionText) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    Page<Question> searchForAdmin(@Param("category") String category,
                                  @Param("difficulty") String difficulty,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);

    @Query("SELECT q.category, COUNT(q) FROM Question q GROUP BY q.category")
    List<Object[]> countQuestionsGroupedByCategory();

    @Query("SELECT q.difficulty, COUNT(q) FROM Question q GROUP BY q.difficulty")
    List<Object[]> countQuestionsGroupedByDifficulty();

    @Query("SELECT q.category, q.difficulty, COUNT(q) FROM Question q GROUP BY q.category, q.difficulty")
    List<Object[]> countQuestionsGroupedByCategoryAndDifficulty();

    /** Used by the seeder to avoid inserting the same question twice. */
    @Query("SELECT q.questionText FROM Question q")
    List<String> findAllQuestionTexts();
}
