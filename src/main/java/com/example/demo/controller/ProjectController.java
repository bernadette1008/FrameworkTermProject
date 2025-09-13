package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectController {

    @GetMapping("/")
    public String loginForm(Model model){
        return "login";
    }

    @GetMapping("/register-type")
    public String registerTypeForm(Model model){

        return "register-type";
    }

    @GetMapping("/register-student")
    public String registerStudentForm(Model model){

        return "register-student";
    }

    @GetMapping("/register-success")
    public String registerSuccessForm(Model model){

        return "register-success";
    }

    @GetMapping("/register-professor")
    public String registerProfessorForm(Model model){
        return "register-professor";
    }
}
