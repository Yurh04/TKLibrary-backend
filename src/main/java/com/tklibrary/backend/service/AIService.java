package com.tklibrary.backend.service;

import java.util.Map;

public interface AIService {
    Map<String, Object> scoreAnswer(String question, String correctAnswer, String userAnswer);
    Map<String, Object> askQuestion(String question);
}