package com.tklibrary.backend.repository;

import com.tklibrary.backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // 可以根据需要添加自定义查询方法
}