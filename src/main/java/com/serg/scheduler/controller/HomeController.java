package com.serg.scheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//Redirects so user doesn't end on blank page

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "redirect:/courses";
    }
}