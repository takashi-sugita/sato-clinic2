package com.example.sato_clinic2;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        add(registry, "/css/**", "file:./css/");
        add(registry, "/js/**", "file:./js/");
        add(registry, "/img/**", "file:./img/");
        add(registry, "/data/**", "file:./data/");
        add(registry, "/about/**", "file:./about/");
        add(registry, "/access/**", "file:./access/");
        add(registry, "/faq/**", "file:./faq/");
        add(registry, "/information/**", "file:./information/");
        add(registry, "/medical/**", "file:./medical/");
        add(registry, "/reservation/**", "file:./reservation/");
        add(registry, "/admin/**", "file:./admin/");
        add(registry, "/docs/**", "file:./docs/");
        registry.addResourceHandler("/**")
                .addResourceLocations("file:./");
    }

    private void add(ResourceHandlerRegistry registry, String pattern, String location) {
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }
}
