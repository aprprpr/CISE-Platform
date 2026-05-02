package com.itheima.service;

import com.itheima.domain.EvaluationScore;

import java.util.Map;

public interface EvaluationService {
    void submitScore(EvaluationScore score);
    Map<String, Object> calculateProjectScore(Integer projectId);
    Map<String, Object> calculatePersonalScore(Integer projectId);
}
