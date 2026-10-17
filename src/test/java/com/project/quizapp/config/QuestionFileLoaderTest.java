package com.project.quizapp.config;

import com.project.quizapp.model.Question;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestionFileLoaderTest {

    private static final String VALID_LINE = "GENERAL|EASY|ข้อสอบ 1|A|B|C|D|2|คำอธิบาย";

    private static List<Question> parse(String text) throws Exception {
        return QuestionFileLoader.parse(new StringReader(text));
    }

    private static IllegalStateException failsWith(String text) {
        return assertThrows(IllegalStateException.class, () -> parse(text));
    }

    @Test
    void shouldParseValidLinesAndIgnoreCommentsAndBlankLines() throws Exception {
        List<Question> questions = parse("# comment\n\n" + VALID_LINE + "\n");

        assertEquals(1, questions.size());
        Question q = questions.get(0);
        assertEquals("GENERAL", q.getCategory());
        assertEquals("EASY", q.getDifficulty());
        assertEquals("ข้อสอบ 1", q.getQuestionText());
        assertEquals(List.of("A", "B", "C", "D"), q.getOptions());
        assertEquals(2, q.getCorrectIndex());
        assertEquals("คำอธิบาย", q.getExplanation());
    }

    @Test
    void shouldRejectWrongNumberOfFields() {
        assertTrue(failsWith("GENERAL|EASY|only three").getMessage().contains("line 1"));
    }

    @Test
    void shouldRejectUnknownCategoryOrDifficulty() {
        assertTrue(failsWith("SPORTS|EASY|Q|A|B|C|D|0|E").getMessage().contains("category"));
        assertTrue(failsWith("GENERAL|IMPOSSIBLE|Q|A|B|C|D|0|E").getMessage().contains("difficulty"));
    }

    @Test
    void shouldRejectBadCorrectIndex() {
        assertTrue(failsWith("GENERAL|EASY|Q|A|B|C|D|4|E").getMessage().contains("between 0 and 3"));
        assertTrue(failsWith("GENERAL|EASY|Q|A|B|C|D|x|E").getMessage().contains("not a number"));
    }

    @Test
    void shouldRejectDuplicateOptionsAndDuplicateQuestions() {
        assertTrue(failsWith("GENERAL|EASY|Q|A|A|C|D|0|E").getMessage().contains("different"));
        assertTrue(failsWith(VALID_LINE + "\n" + VALID_LINE).getMessage().contains("line 2"));
    }

    @Test
    void bundledQuestionBankIsValidAndCoversEveryCategoryAndDifficulty() throws Exception {
        List<Question> questions;
        try (Reader reader = new InputStreamReader(
                new ClassPathResource(DataSeeder.QUESTIONS_RESOURCE).getInputStream(), StandardCharsets.UTF_8)) {
            questions = QuestionFileLoader.parse(reader);
        }

        for (String category : QuestionFileLoader.CATEGORIES) {
            for (String difficulty : QuestionFileLoader.DIFFICULTIES) {
                long count = questions.stream()
                        .filter(q -> q.getCategory().equals(category) && q.getDifficulty().equals(difficulty))
                        .count();
                assertTrue(count >= 5, category + "/" + difficulty + " has only " + count + " questions");
            }
        }

        Set<String> texts = new HashSet<>();
        questions.forEach(q -> assertTrue(texts.add(q.getQuestionText()), "duplicate: " + q.getQuestionText()));
    }
}
