package com.itheima.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/evaluation")
public class EvaluationPageController {

    @GetMapping("/score")
    public String getScorePage(Model model, HttpSession session) {
        Integer studentId = (Integer) session.getAttribute("studentid");
        System.out.println("评分页面 - 从session获取的studentId: " + studentId);
        if (studentId == null) {
            System.out.println("studentId为null，重定向到登录页面");
            return "redirect:/login";
        }
        model.addAttribute("studentId", studentId);
        System.out.println("评分页面 - 传递给前端的studentId: " + studentId);
        return "evaluation/score";
    }

    @GetMapping("/score/{projectId}")
    public String getScorePageByProject(@PathVariable Integer projectId, Model model, HttpSession session) {
        Integer studentId = (Integer) session.getAttribute("studentid");
        System.out.println("评分页面(projectId:" + projectId + ") - 从session获取的studentId: " + studentId);
        if (studentId == null) {
            System.out.println("studentId为null，重定向到登录页面");
            return "redirect:/login";
        }
        model.addAttribute("studentId", studentId);
        model.addAttribute("projectId", projectId);
        System.out.println("评分页面(projectId:" + projectId + ") - 传递给前端的studentId: " + studentId);
        return "evaluation/score";
    }
}