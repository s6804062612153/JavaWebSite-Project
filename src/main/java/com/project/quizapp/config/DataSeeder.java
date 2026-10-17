package com.project.quizapp.config;

import com.project.quizapp.model.Question;
import com.project.quizapp.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads the question bank from {@code data/questions.txt} into the database on start-up.
 *
 * It only inserts questions whose text is not already stored, so it is safe to run on
 * every start: an in-memory database is filled from scratch, while a persistent database
 * (file H2 / PostgreSQL) just receives the new lines added to the file.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    static final String QUESTIONS_RESOURCE = "data/questions.txt";

    @Bean
    CommandLineRunner initDatabase(QuestionRepository repository) {
        return args -> {
            List<Question> fromFile;
            try (Reader reader = new InputStreamReader(
                    new ClassPathResource(QUESTIONS_RESOURCE).getInputStream(), StandardCharsets.UTF_8)) {
                fromFile = QuestionFileLoader.parse(reader);
            }

            Set<String> existingTexts = new HashSet<>(repository.findAllQuestionTexts());
            List<Question> toInsert = fromFile.stream()
                    .filter(q -> !existingTexts.contains(q.getQuestionText()))
                    .toList();

            if (!toInsert.isEmpty()) {
                repository.saveAll(toInsert);
            }
            log.info("Question bank: {} in file, {} added, {} total in database",
                    fromFile.size(), toInsert.size(), repository.count());
        };
    }
}
