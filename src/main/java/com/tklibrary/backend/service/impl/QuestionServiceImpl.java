package com.tklibrary.backend.service.impl;

import com.tklibrary.backend.model.Question;
import com.tklibrary.backend.repository.QuestionRepository;
import com.tklibrary.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionServiceImpl implements QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Override
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }
    
    @Override
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }
    
    @Override
    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }
    
    @Override
    public Question updateQuestion(Long id, Question questionDetails) {
        Optional<Question> optionalQuestion = questionRepository.findById(id);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            question.setContent(questionDetails.getContent());
            question.setAnswer(questionDetails.getAnswer());
            question.setSubject(questionDetails.getSubject());
            return questionRepository.save(question);
        }
        return null;
    }
    
    @Override
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
}