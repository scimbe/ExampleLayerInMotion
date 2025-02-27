package com.example.motion.visual.particle;

import lombok.Data;

/**
 * Konfiguration für Partikel und Emitter im Partikelsystem.
 * Ermöglicht die flexible Anpassung der Partikeleigenschaften.
 */
@Data
public class ParticleConfig {
    // Emitter-Eigenschaften
    private int emissionRate = 5;  // Partikel pro Sekunde
    
    // Partikel-Größe
    private float minSize = 0.1f;
    private float maxSize = 0.3f;
    
    // Partikel-Geschwindigkeit
    private float minSpeed = 0.5f;
    private float maxSpeed = 1.5f;
    
    // Richtungsverteilung der Partikel in Grad (z.B. 360 für alle Richtungen)
    private float spread = 30.0f;
    
    // Lebensdauer der Partikel in Millisekunden
    private long particleLifetime = 2000;
    
    // Physikalische Eigenschaften
    private float gravity = 0.0f;
    
    // Farbe und Transparenz
    private float red = 1.0f;
    private float green = 1.0f;
    private float blue = 1.0f;
    private float startAlpha = 1.0f;
    private float endAlpha = 0.0f;
    
    /**
     * Setzt die Partikelgröße.
     * 
     * @param min Minimale Größe
     * @param max Maximale Größe
     */
    public void setParticleSize(float min, float max) {
        this.minSize = min;
        this.maxSize = max;
    }
    
    /**
     * Setzt die Partikelgeschwindigkeit.
     * 
     * @param min Minimale Geschwindigkeit
     * @param max Maximale Geschwindigkeit
     */
    public void setParticleSpeed(float min, float max) {
        this.minSpeed = min;
        this.maxSpeed = max;
    }
    
    /**
     * Setzt die Partikelfarbe (RGB-Werte zwischen 0 und 1).
     * 
     * @param r Rot-Komponente
     * @param g Grün-Komponente
     * @param b Blau-Komponente
     */
    public void setColor(float r, float g, float b) {
        this.red = clamp(r, 0, 1);
        this.green = clamp(g, 0, 1);
        this.blue = clamp(b, 0, 1);
    }
    
    /**
     * Setzt die Partikelfarbe aus einem hexadezimalen Farbwert.
     * 
     * @param hexColor Farbwert im Format "#RRGGBB" oder "RRGGBB"
     */
    public void setColorFromHex(String hexColor) {
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }
        
        try {
            int colorValue = Integer.parseInt(hexColor, 16);
            
            float r = ((colorValue >> 16) & 0xFF) / 255.0f;
            float g = ((colorValue >> 8) & 0xFF) / 255.0f;
            float b = (colorValue & 0xFF) / 255.0f;
            
            setColor(r, g, b);
        } catch (NumberFormatException e) {
            // Verwende Standardfarbe bei ungültigem Farbwert
            setColor(1.0f, 1.0f, 1.0f);
        }
    }
    
    /**
     * Hilfsmethode zum Begrenzen eines Wertes auf einen bestimmten Bereich.
     */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Erstellt eine Konfiguration für Staubpartikel.
     * 
     * @return Konfiguration für Staubpartikel
     */
    public static ParticleConfig createDustConfig() {
        ParticleConfig config = new ParticleConfig();
        
        config.setEmissionRate(5);
        config.setParticleLifetime(1500);
        config.setParticleSize(0.05f, 0.2f);
        config.setParticleSpeed(0.3f, 0.8f);
        config.setSpread(45.0f);
        config.setGravity(0.1f);
        config.setColor(0.8f, 0.7f, 0.6f); // Bräunlicher Staub
        config.setStartAlpha(0.7f);
        config.setEndAlpha(0.0f);
        
        return config;
    }
    
    /**
     * Erstellt eine Konfiguration für Glitzerpartikel bei Zielerfassung.
     * 
     * @return Konfiguration für Glitzerpartikel
     */
    public static ParticleConfig createGoalReachedConfig() {
        ParticleConfig config = new ParticleConfig();
        
        config.setEmissionRate(0); // Nur Burst, keine kontinuierliche Emission
        config.setParticleLifetime(2000);
        config.setParticleSize(0.08f, 0.2f);
        config.setParticleSpeed(0.5f, 2.0f);
        config.setSpread(360.0f); // In alle Richtungen
        config.setGravity(-0.05f); // Leicht aufsteigend
        config.setColor(1.0f, 0.8f, 0.2f); // Goldenes Glitzern
        config.setStartAlpha(1.0f);
        config.setEndAlpha(0.0f);
        
        return config;
    }
    
    /**
     * Erstellt eine Konfiguration für Partikel beim Layerwechsel.
     * 
     * @param layerType Typ des Bewegungslayers
     * @return Konfiguration für Layerwechsel-Partikel
     */
    public static ParticleConfig createLayerTransitionConfig(String layerType) {
        ParticleConfig config = new ParticleConfig();
        
        config.setEmissionRate(30); // Kurzer, intensiver Burst
        config.setParticleLifetime(1000);
        config.setParticleSize(0.1f, 0.3f);
        config.setParticleSpeed(0.2f, 1.0f);
        config.setSpread(180.0f); // Halbkreisförmige Verteilung
        config.setGravity(0.0f);
        
        // Farbe basierend auf Layer-Typ
        switch (layerType) {
            case "RunningLayer":
                config.setColor(1.0f, 0.3f, 0.3f); // Rötlich
                break;
            case "AdvancedWalkingLayer":
                config.setColor(0.4f, 0.7f, 0.9f); // Bläulich
                break;
            case "IdleLayer":
                config.setColor(0.2f, 0.8f, 0.4f); // Grünlich
                break;
            default: // BasicWalkingLayer und andere
                config.setColor(0.7f, 0.7f, 0.7f); // Gräulich
                break;
        }
        
        config.setStartAlpha(0.8f);
        config.setEndAlpha(0.0f);
        
        return config;
    }
}
