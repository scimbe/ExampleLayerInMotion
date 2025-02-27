package com.example.motion.visual.particle;

import lombok.Data;
import java.util.UUID;

/**
 * Repräsentiert ein einzelnes Partikel im Partikelsystem.
 * Enthält Position, Bewegung, visuelle Eigenschaften und Lebensdauer.
 */
@Data
public class Particle {
    // Identifikation
    private final UUID id;
    
    // Position
    private float x;
    private float y;
    private float z;
    
    // Geschwindigkeit
    private float velocityX;
    private float velocityY;
    private float velocityZ;
    
    // Beschleunigung (z.B. für Gravitation)
    private float accelerationX;
    private float accelerationY;
    private float accelerationZ;
    
    // Visuelle Eigenschaften
    private final float size;  // Größe des Partikels
    private final float startAlpha; // Anfangsopazität
    private final float endAlpha;   // Endopazität
    private final float red;   // Rot-Komponente (0-1)
    private final float green; // Grün-Komponente (0-1)
    private final float blue;  // Blau-Komponente (0-1)
    
    // Lebensdauer
    private final long creationTime; // Erstellungszeit in Millisekunden
    private final long lifetime;     // Lebensdauer in Millisekunden
    
    /**
     * Erstellt ein neues Partikel mit vollständigen Parametern.
     */
    public Particle(UUID id, float x, float y, float z, 
                   float velocityX, float velocityY, float velocityZ,
                   float accelerationX, float accelerationY, float accelerationZ,
                   float size, float startAlpha, float endAlpha,
                   float red, float green, float blue,
                   long creationTime, long lifetime) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.accelerationX = accelerationX;
        this.accelerationY = accelerationY;
        this.accelerationZ = accelerationZ;
        this.size = size;
        this.startAlpha = startAlpha;
        this.endAlpha = endAlpha;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.creationTime = creationTime;
        this.lifetime = lifetime;
    }
    
    /**
     * Aktualisiert die Position des Partikels basierend auf seiner Geschwindigkeit und Beschleunigung.
     * 
     * @param deltaTime Zeit seit dem letzten Update in Sekunden
     */
    public void update(float deltaTime) {
        // Aktualisiere Geschwindigkeit basierend auf Beschleunigung
        velocityX += accelerationX * deltaTime;
        velocityY += accelerationY * deltaTime;
        velocityZ += accelerationZ * deltaTime;
        
        // Aktualisiere Position basierend auf Geschwindigkeit
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        z += velocityZ * deltaTime;
    }
    
    /**
     * Berechnet die aktuelle Opazität basierend auf der verstrichenen Zeit.
     * Das Partikel wird mit der Zeit durchsichtiger.
     * 
     * @return Aktuelle Opazität zwischen 0.0 und 1.0
     */
    public float getCurrentAlpha() {
        // Berechne den aktuellen Lebenszeitfaktor (0.0 bis 1.0)
        float lifetimeFactor = getLifetimeProgress();
        
        // Interpoliere zwischen Start- und Endopazität
        return startAlpha + (endAlpha - startAlpha) * lifetimeFactor;
    }
    
    /**
     * Gibt den Fortschritt der Lebensdauer des Partikels zurück.
     * 
     * @return Wert zwischen 0.0 (neu erstellt) und 1.0 (Lebensdauer erreicht)
     */
    public float getLifetimeProgress() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - creationTime;
        
        return Math.min(1.0f, (float) elapsedTime / lifetime);
    }
    
    /**
     * Prüft, ob das Partikel seine Lebensdauer überschritten hat.
     * 
     * @return true wenn die Lebensdauer überschritten wurde
     */
    public boolean isExpired() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - creationTime) >= lifetime;
    }
    
    /**
     * Gibt die verbleibende Lebensdauer in Millisekunden zurück.
     * 
     * @return Verbleibende Lebensdauer in Millisekunden, oder 0 wenn abgelaufen
     */
    public long getRemainingLifetime() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - creationTime;
        
        return Math.max(0, lifetime - elapsedTime);
    }
    
    /**
     * Erstellt eine HTML/CSS-Farbdarstellung des Partikels.
     * 
     * @return CSS-Farbe im rgba-Format
     */
    public String getCssColor() {
        int r = Math.min(255, Math.max(0, (int)(red * 255)));
        int g = Math.min(255, Math.max(0, (int)(green * 255)));
        int b = Math.min(255, Math.max(0, (int)(blue * 255)));
        float a = getCurrentAlpha();
        
        return String.format("rgba(%d, %d, %d, %.2f)", r, g, b, a);
    }
}
