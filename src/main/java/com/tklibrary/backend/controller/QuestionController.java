package com.tklibrary.backend.controller;

import com.tklibrary.backend.model.Question;
import com.tklibrary.backend.service.AIService;
import com.tklibrary.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private AIService aiService;
    
    // 保存题目
    @PostMapping("/addQuestions")
    public ResponseEntity<Question> saveQuestion(@RequestBody Question question) {
        Question savedQuestion = questionService.saveQuestion(question);
        return new ResponseEntity<>(savedQuestion, HttpStatus.CREATED);
    }
    
    // 获取题目列表
    @GetMapping("/fetchQuestions")
    public ResponseEntity<List<Question>> getAllQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }
    
    // 获取题目详情
    @GetMapping("/questions/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long id) {
        Optional<Question> question = questionService.getQuestionById(id);
        return question.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // 编辑题目
    @PostMapping("/editQuestions")
    public ResponseEntity<Question> updateQuestion(@RequestBody Question question) {
        if (question.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Question updatedQuestion = questionService.updateQuestion(question.getId(), question);
        if (updatedQuestion != null) {
            return ResponseEntity.ok(updatedQuestion);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // AI打分
    @PostMapping("/questions/score")
    public ResponseEntity<Map<String, Object>> scoreQuestion(@RequestBody Map<String, Object> request) {
        try {
            Long id = Long.parseLong(request.get("id").toString());
            Optional<Question> optionalQuestion = questionService.getQuestionById(id);
            
            if (optionalQuestion.isPresent()) {
                Question question = optionalQuestion.get();
                String userAnswer = request.get("answer").toString();
                
                Map<String, Object> scoreResult = aiService.scoreAnswer(
                        question.getContent(),
                        question.getAnswer(),
                        userAnswer
                );
                
                return ResponseEntity.ok(scoreResult);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // AI问答
    @PostMapping("/questions/ask")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody Map<String, Object> request) {
        try {
            String question = request.get("question").toString();
            Map<String, Object> answerResult = aiService.askQuestion(question);
            return ResponseEntity.ok(answerResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}