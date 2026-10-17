package com.project.quizapp.config;

import com.project.quizapp.model.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads the question bank (data/questions.txt) and validates every line.
 *
 * Format (one question per line, fields separated by "|"):
 * CATEGORY|DIFFICULTY|QUESTION|OPTION_0|OPTION_1|OPTION_2|OPTION_3|CORRECT_INDEX|EXPLANATION
 *
 * Empty lines and lines starting with "#" are ignored. Any malformed line makes
 * the whole load fail with a message that names the line number, so bad data is
 * caught at start-up (and in the unit test) instead of showing up in a quiz.
 */
public final class QuestionFileLoader {

    public static final Set<String> CATEGORIES = Set.of("ANIME_GAMING", "GENERAL");
    public static final Set<String> DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");

    static final int OPTION_COUNT = 4;
    private static final int FIELD_COUNT = 3 + OPTION_COUNT + 2;

    private QuestionFileLoader() {
    }

    public static List<Question> parse(Reader reader) throws IOException {
        List<Question> questions = new ArrayList<>();
        Set<String> seenTexts = new HashSet<>();

        try (BufferedReader in = new BufferedReader(reader)) {
            String line;
            int lineNumber = 0;
            while ((line = in.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);   // tolerate a UTF-8 BOM
                }
                if (line.isBlank() || line.stripLeading().startsWith("#")) {
                    continue;
                }

                Question question = parseLine(line, lineNumber);
                if (!seenTexts.add(question.getQuestionText())) {
                    throw error(lineNumber, "duplicate question text");
                }
                questions.add(question);
            }
        }
        return questions;
    }

    private static Question parseLine(String line, int lineNumber) {
        String[] fields = line.split("\\|", -1);
        if (fields.length != FIELD_COUNT) {
            throw error(lineNumber, "expected " + FIELD_COUNT + " fields separated by '|' but found " + fields.length);
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
            if (fields[i].isEmpty()) {
                throw error(lineNumber, "field " + (i + 1) + " is empty");
            }
        }

        String category = fields[0].toUpperCase();
        String difficulty = fields[1].toUpperCase();
        if (!CATEGORIES.contains(category)) {
            throw error(lineNumber, "unknown category '" + fields[0] + "' (allowed: " + CATEGORIES + ")");
        }
        if (!DIFFICULTIES.contains(difficulty)) {
            throw error(lineNumber, "unknown difficulty '" + fields[1] + "' (allowed: " + DIFFICULTIES + ")");
        }

        List<String> options = new ArrayList<>(List.of(fields[3], fields[4], fields[5], fields[6]));
        if (new HashSet<>(options).size() != OPTION_COUNT) {
            throw error(lineNumber, "the four options must all be different");
        }

        int correctIndex;
        try {
            correctIndex = Integer.parseInt(fields[7]);
        } catch (NumberFormatException e) {
            throw error(lineNumber, "correct index '" + fields[7] + "' is not a number");
        }
        if (correctIndex < 0 || correctIndex >= OPTION_COUNT) {
            throw error(lineNumber, "correct index must be between 0 and " + (OPTION_COUNT - 1));
        }

        return new Question(null, category, difficulty, fields[2], options, correctIndex, fields[8]);
    }

    private static IllegalStateException error(int lineNumber, String message) {
        return new IllegalStateException("Invalid question data at line " + lineNumber + ": " + message);
    }
}
