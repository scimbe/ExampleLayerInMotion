package com.example.motion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web-Konfiguration für statische Ressourcen und Pfad-Zuordnungen.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Statische Ressourcen konfigurieren
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600) // 1 Stunde
                .resourceChain(true);
                
        // Swagger UI Ressourcen
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(true);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Hauptseite
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/game").setViewName("forward:/index.html");
        
        // Swagger UI Umleitung
        registry.addViewController("/swagger").setViewName("redirect:/swagger-ui/index.html");
    }
}