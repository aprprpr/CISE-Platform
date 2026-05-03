package com.itheima.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/recommend")
public class RecommendPageController {

    @GetMapping("/index")
    public String getRecommendPage(Model model, HttpSession session) {
        Integer studentId = (Integer) session.getAttribute("studentid");
        if (studentId == null) {
            return "redirect:/login";
        }
        model.addAttribute("studentId", studentId);
        return "Student/recommend";
    }
}