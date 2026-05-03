package com.itheima.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/teacher")
public class TeacherEvaluationController {

    @GetMapping("/score")
    public String getScorePage(Model model, HttpSession session) {
        Integer teacherId = (Integer) session.getAttribute("teacherid");
        System.out.println("教师评分页面 - 从session获取的teacherId: " + teacherId);
        if (teacherId == null) {
            System.out.println("teacherId为null，重定向到登录页面");
            return "redirect:/login";
        }
        model.addAttribute("teacherId", teacherId);
        System.out.println("教师评分页面 - 传递给前端的teacherId: " + teacherId);
        return "teacher/score";
    }

    @GetMapping("/score/{projectId}")
    public String getScorePageByProject(@PathVariable Integer projectId, Model model, HttpSession session) {
        Integer teacherId = (Integer) session.getAttribute("teacherid");
        System.out.println("教师评分页面(projectId:" + projectId + ") - 从session获取的teacherId: " + teacherId);
        if (teacherId == null) {
            System.out.println("teacherId为null，重定向到登录页面");
            return "redirect:/login";
        }
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("projectId", projectId);
        System.out.println("教师评分页面(projectId:" + projectId + ") - 传递给前端的teacherId: " + teacherId);
        return "teacher/score";
    }
}