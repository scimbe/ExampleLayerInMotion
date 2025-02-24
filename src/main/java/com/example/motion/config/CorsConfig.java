package com.example.motion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * CORS-Konfiguration für den API-Zugriff.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Für Entwicklungszwecke erlauben wir alle Ursprünge
        // In der Produktion sollte dies eingeschränkt werden
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Zugriffssteuerung für Cookies und Authentifizierungs-Header
        config.setAllowCredentials(true);
        
        // Expose Header für besseres Caching und Pagination
        config.setExposedHeaders(Arrays.asList(
            "Authorization", "X-Total-Count", "Link", 
            "X-Rate-Limit-Remaining", "X-Rate-Limit-Retry-After-Milliseconds"
        ));
        
        // Max Age für Preflight-Requests (OPTIONS)
        config.setMaxAge(3600L);
        
        // Konfiguration auf alle Pfade anwenden
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}