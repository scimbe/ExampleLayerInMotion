package com.example.motion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS-Konfiguration für den API-Zugriff.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Alle Ursprünge erlauben für Entwicklungszwecke
        // In der Produktion sollte dies eingeschränkt werden
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        
        // Expose Header für besseres Caching und Pagination
        config.setExposedHeaders(Arrays.asList(
            "Authorization", "X-Total-Count", "Link", 
            "X-Rate-Limit-Remaining", "X-Rate-Limit-Retry-After-Milliseconds"
        ));
        
        // Konfiguration auf alle Pfade anwenden
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}