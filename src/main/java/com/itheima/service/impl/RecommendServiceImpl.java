package com.itheima.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itheima.dao.ProjectDao;
import com.itheima.domain.Project;
import com.itheima.service.RecommendService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private ProjectDao projectDao;

    @Value("${doubao.api.key}")
    private String apiKey;

    @Value("${doubao.api.url:https://api.doubao.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${doubao.api.model:doubao-3}")
    private String modelName;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public List<Map<String, Object>> recommendProjects(Integer userId, Integer topN) {
        List<Project> allProjects = projectDao.selectList(null);
        if (allProjects.isEmpty()) {
            return Collections.emptyList();
        }

        String projectList = formatProjectsForPrompt(allProjects);
        String prompt = buildUserRecommendPrompt(userId, projectList, topN);

        try {
            String response = callDoubaoApi(prompt);
            return parseRecommendResponse(response, allProjects);
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackRecommend(topN);
        }
    }

    @Override
    public List<Map<String, Object>> recommendByKeywords(String keywords, Integer topN) {
        List<Project> allProjects = projectDao.selectList(null);
        if (allProjects.isEmpty()) {
            return Collections.emptyList();
        }

        String projectList = formatProjectsForPrompt(allProjects);
        String prompt = buildKeywordRecommendPrompt(keywords, projectList, topN);

        try {
            String response = callDoubaoApi(prompt);
            return parseRecommendResponse(response, allProjects);
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackRecommend(topN);
        }
    }

    @Override
    public List<Map<String, Object>> recommendSimilarProjects(Integer projectId, Integer topN) {
        Project targetProject = projectDao.selectById(projectId);
        if (targetProject == null) {
            return Collections.emptyList();
        }

        List<Project> allProjects = projectDao.selectList(null);
        allProjects.removeIf(p -> p.getProjectId().equals(projectId));

        if (allProjects.isEmpty()) {
            return Collections.emptyList();
        }

        String projectList = formatProjectsForPrompt(allProjects);
        String description = targetProject.getProjectInfo() != null ? targetProject.getProjectInfo() : "暂无描述";
        String prompt = buildSimilarRecommendPrompt(targetProject.getProjectName(), description, projectList, topN);

        try {
            String response = callDoubaoApi(prompt);
            return parseRecommendResponse(response, allProjects);
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackRecommend(topN);
        }
    }

    private String formatProjectsForPrompt(List<Project> projects) {
        StringBuilder sb = new StringBuilder();
        for (Project project : projects) {
            String info = project.getProjectInfo() != null ? project.getProjectInfo() : "暂无描述";
            sb.append(String.format("项目%d: %s - %s\n", project.getProjectId(), project.getProjectName(), info));
        }
        return sb.toString();
    }

    private String buildUserRecommendPrompt(Integer userId, String projectList, Integer topN) {
        return String.format(
            "你是一个大学生创新创业项目推荐专家。请根据用户的需求推荐最合适的项目。\n\n" +
            "可用项目列表：\n%s\n" +
            "用户ID: %d\n" +
            "用户正在寻找合适的创新创业项目。请基于项目的创新性、实用性和市场潜力，推荐最适合该用户的%d个项目。\n\n" +
            "请仅返回项目编号列表，格式为：[项目编号1, 项目编号2, ...]，不要添加任何额外内容。",
            projectList, userId, topN
        );
    }

    private String buildKeywordRecommendPrompt(String keywords, String projectList, Integer topN) {
        return String.format(
            "你是一个大学生创新创业项目推荐专家。请根据关键词推荐最合适的项目。\n\n" +
            "可用项目列表：\n%s\n" +
            "用户搜索关键词: %s\n" +
            "请推荐与这些关键词最相关的%d个项目。\n\n" +
            "请仅返回项目编号列表，格式为：[项目编号1, 项目编号2, ...]，不要添加任何额外内容。",
            projectList, keywords, topN
        );
    }

    private String buildSimilarRecommendPrompt(String projectName, String description, String projectList, Integer topN) {
        return String.format(
            "你是一个大学生创新创业项目推荐专家。请推荐与给定项目相似的项目。\n\n" +
            "参考项目：\n项目名称: %s\n项目描述: %s\n\n" +
            "可用项目列表：\n%s\n" +
            "请推荐与参考项目最相似的%d个项目。\n\n" +
            "请仅返回项目编号列表，格式为：[项目编号1, 项目编号2, ...]，不要添加任何额外内容。",
            projectName, description, projectList, topN
        );
    }

    private String callDoubaoApi(String prompt) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", modelName);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 2048);
        requestBody.put("temperature", 0.7);
        requestBody.put("top_p", 0.9);

        RequestBody body = RequestBody.create(
            requestBody.toJSONString(),
            MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API调用失败: " + response);
            }
            return response.body().string();
        }
    }

    private List<Map<String, Object>> parseRecommendResponse(String response, List<Project> allProjects) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            JSONObject json = JSON.parseObject(response);
            String content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

            List<Integer> projectIds = parseProjectIds(content);

            for (Integer id : projectIds) {
                for (Project project : allProjects) {
                    if (project.getProjectId().equals(id)) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("projectId", project.getProjectId());
                        item.put("projectName", project.getProjectName());
                        item.put("projectInfo", project.getProjectInfo());
                        item.put("teacherId", project.getTeacherId());
                        results.add(item);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private List<Integer> parseProjectIds(String content) {
        List<Integer> ids = new ArrayList<>();

        int start = content.indexOf('[');
        int end = content.indexOf(']');

        if (start >= 0 && end > start) {
            String arrayStr = content.substring(start + 1, end);
            String[] parts = arrayStr.split(",");
            for (String part : parts) {
                try {
                    ids.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException e) {
                    String numStr = part.replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        ids.add(Integer.parseInt(numStr));
                    }
                }
            }
        }

        return ids;
    }

    private List<Map<String, Object>> fallbackRecommend(Integer topN) {
        List<Project> projects = projectDao.selectList(null);
        Collections.shuffle(projects);

        List<Map<String, Object>> results = new ArrayList<>();
        int count = Math.min(topN, projects.size());

        for (int i = 0; i < count; i++) {
            Project project = projects.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("projectId", project.getProjectId());
            item.put("projectName", project.getProjectName());
            item.put("projectInfo", project.getProjectInfo());
            item.put("teacherId", project.getTeacherId());
            results.add(item);
        }

        return results;
    }
}