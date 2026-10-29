package com.example.sato_clinic2;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    @GetMapping({"/reservation", "/reservation/"})
    public String reservation() {
        return "redirect:/reservation/index.html";
    }

    @GetMapping({"/admin", "/admin/"})
    public String admin() {
        return "redirect:/admin/index.html";
    }
}
