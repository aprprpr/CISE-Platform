package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.dao.EvaluationScoreMapper;
import com.itheima.dao.ProjectMemberContributionMapper;
import com.itheima.domain.EvaluationScore;
import com.itheima.domain.ProjectMemberContribution;
import com.itheima.service.EvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationServiceImpl.class);

    @Autowired
    private EvaluationScoreMapper evaluationScoreMapper;

    @Autowired
    private ProjectMemberContributionMapper projectMemberContributionMapper;

    @Override
    public void submitScore(EvaluationScore score) {
        QueryWrapper<EvaluationScore> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", score.getProjectId())
                .eq("user_id", score.getUserId())
                .eq("score_type", score.getScoreType());

        EvaluationScore existingScore = evaluationScoreMapper.selectOne(queryWrapper);
        if (existingScore != null) {
            throw new RuntimeException("该用户已对该项目进行过此类评分，请勿重复提交！");
        }

        score.setCreateTime(new Date());
        evaluationScoreMapper.insert(score);
    }

    @Override
    public Map<String, Object> calculateProjectScore(Integer projectId) {
        final double SELF_WEIGHT = 0.2;
        final double TEAM_WEIGHT = 0.3;
        final double MENTOR_WEIGHT = 0.4;
        final double EXTERNAL_WEIGHT = 0.1;

        QueryWrapper<EvaluationScore> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId);
        List<EvaluationScore> scoreList = evaluationScoreMapper.selectList(queryWrapper);

        Map<String, Object> result = new HashMap<>();

        if (scoreList == null || scoreList.isEmpty()) {
            result.put("totalScore", 0);
            result.put("message", "暂无评分数据");
            return result;
        }

        double selfTotal = 0.0;
        int selfCount = 0;
        double teamTotal = 0.0;
        int teamCount = 0;
        double mentorTotal = 0.0;
        int mentorCount = 0;
        double externalTotal = 0.0;
        int externalCount = 0;

        for (EvaluationScore score : scoreList) {
            if (score.getScore() != null) {
                String type = score.getScoreType();
                if (type == null) {
                    continue;
                }
                switch (type.toLowerCase()) {
                    case "self":
                        selfTotal += score.getScore();
                        selfCount++;
                        break;
                    case "team":
                        teamTotal += score.getScore();
                        teamCount++;
                        break;
                    case "mentor":
                        mentorTotal += score.getScore();
                        mentorCount++;
                        break;
                    case "external":
                        externalTotal += score.getScore();
                        externalCount++;
                        break;
                    default:
                        break;
                }
            }
        }

        double self = selfCount > 0 ? selfTotal / selfCount : 0.0;
        double team = teamCount > 0 ? teamTotal / teamCount : 0.0;
        double mentor = mentorCount > 0 ? mentorTotal / mentorCount : 0.0;
        double external = externalCount > 0 ? externalTotal / externalCount : 0.0;
        double totalScore = self * SELF_WEIGHT + team * TEAM_WEIGHT + mentor * MENTOR_WEIGHT
                + external * EXTERNAL_WEIGHT;

        result.put("totalScore", Math.round(totalScore * 100.0) / 100.0);
        result.put("self", Math.round(self * 100.0) / 100.0);
        result.put("team", Math.round(team * 100.0) / 100.0);
        result.put("mentor", Math.round(mentor * 100.0) / 100.0);
        result.put("external", Math.round(external * 100.0) / 100.0);

        return result;
    }

    @Override
    public Map<String, Object> calculatePersonalScore(Integer projectId) {
        Map<String, Object> result = new HashMap<>();
        Map<Integer, Double> data = new HashMap<>();

        Map<String, Object> projectScoreResult = calculateProjectScore(projectId);
        if (projectScoreResult.containsKey("message")) {
            logger.warn("项目暂无评分，无法计算个人得分，项目ID: {}", projectId);
            result.put("data", data);
            result.put("message", "暂无评分");
            return result;
        }

        double totalScore = ((Number) projectScoreResult.get("totalScore")).doubleValue();

        QueryWrapper<ProjectMemberContribution> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId);
        List<ProjectMemberContribution> memberList = projectMemberContributionMapper.selectList(queryWrapper);

        if (memberList == null || memberList.isEmpty()) {
            result.put("data", data);
            result.put("message", "暂无贡献数据");
            return result;
        }

        double totalContributionRate = 0.0;
        for (ProjectMemberContribution member : memberList) {
            Double contributionRate = member.getContributionRate();
            if (contributionRate != null) {
                totalContributionRate += contributionRate;
            }
        }

        if (totalContributionRate < 0.99 || totalContributionRate > 1.01) {
            logger.warn("项目 {} 的贡献系数总和为 {}，不在 0.99 ~ 1.01 之间", projectId, totalContributionRate);
        }

        for (ProjectMemberContribution member : memberList) {
            Integer userId = member.getUserId();
            Double contributionRate = member.getContributionRate();
            if (userId != null && contributionRate != null && contributionRate > 0) {
                double personalScore = totalScore * contributionRate;
                double formattedScore = Math.round(personalScore * 100.0) / 100.0;
                data.put(userId, formattedScore);
            }
        }

        result.put("data", data);
        result.put("message", "计算成功，共计算" + data.size() + "名成员得分");
        return result;
    }
}
