package com.itheima.controller;

import com.itheima.dao.ProjectMemberContributionMapper;
import com.itheima.domain.EvaluationScore;
import com.itheima.domain.ProjectMemberContribution;
import com.itheima.service.EvaluationService;
import com.itheima.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private ProjectMemberContributionMapper projectMemberContributionMapper;

    @PostMapping("/submit")
    public ResponseResult<Void> submitScore(@RequestBody EvaluationScore score) {
        ResponseResult<Void> result = new ResponseResult<>();
        try {
            evaluationService.submitScore(score);
            result.setState(200);
            result.setMessage("评分提交成功！");
        } catch (RuntimeException e) {
            result.setState(500);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @GetMapping("/project/{projectId}")
    public ResponseResult<Map<String, Object>> getProjectScore(@PathVariable Integer projectId) {
        ResponseResult<Map<String, Object>> result = new ResponseResult<>();
        try {
            Map<String, Object> scoreResult = evaluationService.calculateProjectScore(projectId);
            result.setState(200);
            result.setData(scoreResult);
        } catch (Exception e) {
            result.setState(500);
            result.setMessage("查询评分失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/personal/{projectId}")
    public ResponseResult<Map<String, Object>> getPersonalScore(@PathVariable Integer projectId) {
        ResponseResult<Map<String, Object>> result = new ResponseResult<>();
        try {
            Map<String, Object> personalScoreResult = evaluationService.calculatePersonalScore(projectId);
            result.setState(200);
            result.setData(personalScoreResult);
        } catch (Exception e) {
            result.setState(500);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("data", new HashMap<Integer, Double>());
            errorData.put("message", "查询个人评分失败：" + e.getMessage());
            result.setData(errorData);
        }
        return result;
    }

    @PostMapping("/test/addContribution")
    public ResponseResult<Void> addContribution(@RequestParam Integer projectId,
                                                 @RequestParam Integer userId,
                                                 @RequestParam Double contributionRate) {
        ResponseResult<Void> result = new ResponseResult<>();
        try {
            ProjectMemberContribution contribution = new ProjectMemberContribution();
            contribution.setProjectId(projectId);
            contribution.setUserId(userId);
            contribution.setContributionRate(contributionRate);
            projectMemberContributionMapper.insert(contribution);
            result.setState(200);
            result.setMessage("贡献数据添加成功！");
        } catch (Exception e) {
            result.setState(500);
            result.setMessage("添加失败：" + e.getMessage());
        }
        return result;
    }
}
