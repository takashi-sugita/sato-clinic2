package com.example.sato_clinic2;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping({"/", "/index.html"})
    public ResponseEntity<Resource> home() {
        Path indexPath = Paths.get(System.getProperty("user.dir"), "index.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new FileSystemResource(indexPath));
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
