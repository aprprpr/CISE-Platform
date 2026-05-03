package com.itheima.controller;

import com.itheima.dao.ApplyDao;
import com.itheima.dao.ProjectDao;
import com.itheima.domain.Apply;
import com.itheima.domain.Project;
import com.itheima.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
public class ProjectApiController {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ApplyDao applyDao;

    @GetMapping("/list")
    public ResponseResult<List<Project>> getProjectList() {
        ResponseResult<List<Project>> result = new ResponseResult<>();
        try {
            List<Project> projects = projectDao.selectList(null);
            result.setState(200);
            result.setData(projects);
        } catch (Exception e) {
            result.setState(500);
            result.setMessage("查询失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/my-projects")
    public ResponseResult<List<Project>> getMyProjects(@RequestParam(required = false) Integer userId) {
        ResponseResult<List<Project>> result = new ResponseResult<>();
        try {
            System.out.println("my-projects 接口被调用，userId: " + userId);

            if (userId == null) {
                System.out.println("警告: userId 为 null，返回空列表");
                result.setState(200);
                result.setData(new ArrayList<>());
                return result;
            }

            List<Project> projects = new ArrayList<>();

            List<Apply> applies = applyDao.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Apply>()
                    .eq("student_id", userId));

            System.out.println("查询到学生的申报记录数量: " + applies.size() + ", studentId: " + userId);

            for (Apply apply : applies) {
                Project project = projectDao.selectById(apply.getProjectId());
                if (project != null) {
                    projects.add(project);
                }
            }

            System.out.println("查询到学生的项目数量: " + projects.size());
            result.setState(200);
            result.setData(projects);
        } catch (Exception e) {
            System.err.println("查询学生项目失败: " + e.getMessage());
            e.printStackTrace();
            result.setState(500);
            result.setMessage("查询失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/teacher-projects")
    public ResponseResult<List<Project>> getTeacherProjects(@RequestParam(required = false) Integer teacherId) {
        ResponseResult<List<Project>> result = new ResponseResult<>();
        try {
            System.out.println("teacher-projects 接口被调用，teacherId: " + teacherId);

            if (teacherId == null) {
                System.out.println("警告: teacherId 为 null，返回空列表");
                result.setState(200);
                result.setData(new ArrayList<>());
                return result;
            }

            List<Project> projects = projectDao.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Project>()
                            .eq("teacher_id", teacherId)
            );
            System.out.println("查询到教师项目数量: " + projects.size() + ", teacherId: " + teacherId);
            result.setState(200);
            result.setData(projects);
        } catch (Exception e) {
            System.err.println("查询教师项目失败: " + e.getMessage());
            e.printStackTrace();
            result.setState(500);
            result.setMessage("查询失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/adju-projects")
    public ResponseResult<List<Project>> getAdjuProjects(@RequestParam Integer adjuId) {
        ResponseResult<List<Project>> result = new ResponseResult<>();
        try {
            List<Project> projects = new ArrayList<>();

            List<Apply> applies = applyDao.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Apply>()
                            .eq("adju_number", adjuId.toString())
                            .eq("pass_status", "通过")
            );

            for (Apply apply : applies) {
                Project project = projectDao.selectById(apply.getProjectId());
                if (project != null) {
                    projects.add(project);
                }
            }

            result.setState(200);
            result.setData(projects);
        } catch (Exception e) {
            result.setState(500);
            result.setMessage("查询失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/members/{projectId}")
    public ResponseResult<List<Map<String, Object>>> getProjectMembers(@PathVariable Integer projectId) {
        ResponseResult<List<Map<String, Object>>> result = new ResponseResult<>();
        try {
            List<Map<String, Object>> members = projectDao.getProjectMembers(projectId);
            result.setState(200);
            result.setData(members);
        } catch (Exception e) {
            result.setState(500);
            result.setMessage("查询失败：" + e.getMessage());
        }
        return result;
    }
}