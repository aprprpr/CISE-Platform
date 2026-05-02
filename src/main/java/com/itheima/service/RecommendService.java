package com.itheima.service;

import java.util.List;
import java.util.Map;

public interface RecommendService {

    List<Map<String, Object>> recommendProjects(Integer userId, Integer topN);

    List<Map<String, Object>> recommendByKeywords(String keywords, Integer topN);

    List<Map<String, Object>> recommendSimilarProjects(Integer projectId, Integer topN);
}