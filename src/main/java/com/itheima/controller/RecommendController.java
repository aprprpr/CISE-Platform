package com.itheima.controller;

import com.itheima.service.RecommendService;
import com.itheima.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @GetMapping("/projects/{userId}")
    public ResponseResult<List<Map<String, Object>>> recommendProjects(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "10") Integer topN) {
        
        ResponseResult<List<Map<String, Object>>> result = new ResponseResult<>();
        try {
            List<Map<String, Object>> projects = recommendService.recommendProjects(userId, topN);
            result.setState(200);
            result.setData(projects);
            result.setMessage("推荐成功，共" + projects.size() + "个项目");
        } catch (Exception e) {
            result.setState(500);
            result.setMessage("推荐失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/search")
    public ResponseResult<List<Map<String, Object>>> searchProjects(
            @RequestParam String keywords,
            @RequestParam(defaultValue = "10") Integer topN) {
        
        ResponseResult<List<Map<String, Object>>> result = new ResponseResult<>();
        try {
            List<Map<String, Object>> projects = recommendService.recommendByKeywords(keywords, topN);
            result.setState(200);
            result.setData(projects);
            result.setMessage("搜索成功，共" + projects.size() + "个项目");
        } catch (Exception e) {
            result.setState(500);
            result.setMessage("搜索失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/similar/{projectId}")
    public ResponseResult<List<Map<String, Object>>> getSimilarProjects(
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "5") Integer topN) {
        
        ResponseResult<List<Map<String, Object>>> result = new ResponseResult<>();
        try {
            List<Map<String, Object>> projects = recommendService.recommendSimilarProjects(projectId, topN);
            result.setState(200);
            result.setData(projects);
            result.setMessage("查询成功，共" + projects.size() + "个相似项目");
        } catch (Exception e) {
            result.setState(500);
            result.setMessage("查询失败：" + e.getMessage());
        }
        return result;
    }
}