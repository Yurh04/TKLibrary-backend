package com.tklibrary.backend.service;

import com.tklibrary.backend.model.Question;
import java.util.List;
import java.util.Optional;

public interface QuestionService {
    List<Question> getAllQuestions();
    Optional<Question> getQuestionById(Long id);
    Question saveQuestion(Question question);
    Question updateQuestion(Long id, Question questionDetails);
    void deleteQuestion(Long id);
}