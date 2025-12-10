package com.tklibrary.backend.service.impl;

import com.tklibrary.backend.service.AIService;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class AIServiceImpl implements AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);
    
    @Value("${ai.qwen.api.url}")
    private String qwenApiUrl;
    
    @Value("${ai.qwen.model}")
    private String qwenModel;
    
    private String dashscopeApiKey;
    
    // 读取.env文件中的API密钥
    public AIServiceImpl() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(".env")) {
            if (input == null) {
                logger.warn(".env file not found, using system environment variable");
                dashscopeApiKey = System.getenv("DASHSCOPE_API_KEY");
            } else {
                Properties prop = new Properties();
                prop.load(input);
                dashscopeApiKey = prop.getProperty("DASHSCOPE_API_KEY");
                logger.info("Loaded DASHSCOPE_API_KEY from .env file");
            }
        } catch (IOException ex) {
            logger.error("Error loading .env file: {}", ex.getMessage());
            dashscopeApiKey = System.getenv("DASHSCOPE_API_KEY");
        }
    }
    
    @Override
    public Map<String, Object> scoreAnswer(String question, String correctAnswer, String userAnswer) {
        logger.info("AI打分请求 - 题目: {}, 正确答案: {}, 用户答案: {}", question, correctAnswer, userAnswer);
        Map<String, Object> result = new HashMap<>();
        
        // 简化为直接比较答案
        if (userAnswer.equals(correctAnswer)) {
            result.put("score", 100);
            result.put("evaluation", "回答正确");
            result.put("pointsEarned", "1. 答案完全正确");
            result.put("pointsLost", "无");
        } else {
            result.put("score", 0);
            result.put("evaluation", "回答错误");
            result.put("pointsEarned", "无");
            result.put("pointsLost", "1. 答案与标准答案不一致");
        }
        
        result.put("success", true);
        logger.info("AI打分结果: {}", result);
        return result;
    }
    
    // 解析AI的响应文本
    private Map<String, Object> parseAIResponse(String outputText, String correctAnswer, String userAnswer) {
        Map<String, Object> result = new HashMap<>();
        
        logger.info("开始解析AI响应: {}, 正确答案: {}, 用户答案: {}", outputText, correctAnswer, userAnswer);
        
        // 简化解析逻辑，直接根据正确答案和用户答案比较进行评分
        try {
            // 如果用户答案与正确答案完全匹配，给满分
            if (userAnswer.equals(correctAnswer)) {
                result.put("score", 100);
                result.put("evaluation", "用户回答完全正确，与标准答案一致");
                result.put("pointsEarned", "1. 答案完全正确");
                result.put("pointsLost", "无");
            } else if (userAnswer.contains(correctAnswer) || correctAnswer.contains(userAnswer)) {
                // 如果包含正确答案或被包含，给高分
                result.put("score", 80);
                result.put("evaluation", "用户回答基本正确，包含正确答案或与正确答案有部分匹配");
                result.put("pointsEarned", "1. 部分内容正确");
                result.put("pointsLost", "1. 与标准答案不完全一致");
            } else {
                // 否则根据匹配度评分
                // 计算字符串相似度
                double similarity = calculateSimilarity(userAnswer, correctAnswer);
                int score = (int) (similarity * 100);
                result.put("score", score);
                result.put("evaluation", String.format("用户回答与正确答案的匹配度为%d%%", score));
                result.put("pointsEarned", "1. 部分内容与标准答案相似");
                result.put("pointsLost", "1. 与标准答案存在较大差异");
            }
        } catch (Exception e) {
            logger.error("解析AI响应失败: {}", e.getMessage());
            // 如果解析失败，使用默认值
            result.put("score", 0);
            result.put("evaluation", "无法解析AI评价");
            result.put("pointsEarned", "无法解析得分点");
            result.put("pointsLost", "无法解析失分点");
        }
        
        logger.info("解析AI响应结果: {}", result);
        return result;
    }
    
    // 计算两个字符串的相似度（简单实现）
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        if (s1.equals(s2)) {
            return 1.0;
        }
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        // 使用Levenshtein距离计算相似度
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLength;
    }
    
    // 计算Levenshtein距离
    private int levenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[m][n];
    }
    
    @Override
    public Map<String, Object> askQuestion(String question) {
        logger.info("AI问答请求 - 问题: {}", question);
        Map<String, Object> result = new HashMap<>();
        
        // 构建请求参数
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", qwenModel);
        requestBody.put("input", Map.of("prompt", question));
        requestBody.put("parameters", Map.of(
                "temperature", 0.7,
                "top_p", 0.95,
                "max_tokens", 1024
        ));
        
        // 发送请求到Qwen API
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(qwenApiUrl);
            
            // 设置请求头，明确指定UTF-8编码
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.setHeader("Authorization", "Bearer " + dashscopeApiKey);
            
            // 设置请求体，指定UTF-8编码以支持中文
            String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
            StringEntity entity = new StringEntity(jsonBody, java.nio.charset.StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            
            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                // 解析响应时指定UTF-8编码
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info("AI API响应: {}", responseBody);
                
                // 解析响应
                com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
                String outputText = rootNode.path("output").path("text").asText();
                logger.info("AI输出文本: {}", outputText);
                
                result.put("answer", outputText);
                result.put("success", true);
            } catch (ParseException e) {
                logger.error("Failed to parse AI response: {}", e.getMessage());
                result.put("success", false);
                result.put("error", "Failed to parse response: " + e.getMessage());
            }
        } catch (IOException e) {
            logger.error("Failed to call AI API: {}", e.getMessage());
            result.put("success", false);
            result.put("error", "Failed to call AI API: " + e.getMessage());
        }
        
        logger.info("AI问答结果: {}", result);
        return result;
    }
}