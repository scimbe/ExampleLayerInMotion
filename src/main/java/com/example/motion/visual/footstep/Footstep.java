package com.example.motion.visual.footstep;

import com.example.motion.sys.model.Position;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Repräsentiert eine einzelne Fußspur im System.
 * Enthält alle visuellen und Positionsinformationen, die für das Rendering benötigt werden.
 */
@Data
public class Footstep {
    // Identifikation
    private final UUID id;
    private final UUID characterId;
    
    // Position und Orientierung
    private final Position position;
    private final float yawAngle;  // Rotation um die Y-Achse in Grad
    
    // Visuelle Eigenschaften
    private final boolean leftFoot;  // Links- oder Rechtsfuß
    private final float width;      // Breite des Fußabdrucks
    private final float length;     // Länge des Fußabdrucks
    private final float opacity;    // Transparenz (1.0 = vollständig undurchsichtig)
    private final String layerType; // Typ des Bewegungslayers, der diese Fußspur erzeugt hat
    
    // Lebensdauer
    private final Instant creationTime;
    private final long duration;    // Lebensdauer in Millisekunden
    
    /**
     * Berechnet die aktuelle Opazität basierend auf der verstrichenen Zeit.
     * Die Fußspur wird mit der Zeit durchsichtiger.
     * 
     * @return Aktuelle Opazität zwischen 0.0 und dem initialen Opazitätswert
     */
    public float getCurrentOpacity() {
        Instant now = Instant.now();
        long elapsedMillis = now.toEpochMilli() - creationTime.toEpochMilli();
        
        if (elapsedMillis >= duration) {
            return 0.0f;
        }
        
        float fadeFactor = 1.0f - ((float) elapsedMillis / duration);
        return opacity * fadeFactor;
    }
    
    /**
     * Berechnet den Prozentsatz der verbleibenden Lebensdauer.
     * 
     * @return Wert zwischen 0.0 (abgelaufen) und 1.0 (neu erstellt)
     */
    public float getRemainingLifePercentage() {
        Instant now = Instant.now();
        long elapsedMillis = now.toEpochMilli() - creationTime.toEpochMilli();
        
        return Math.max(0.0f, Math.min(1.0f, 1.0f - ((float) elapsedMillis / duration)));
    }
    
    /**
     * Prüft, ob die Fußspur abgelaufen ist.
     * 
     * @return true wenn die Lebensdauer überschritten wurde
     */
    public boolean isExpired() {
        Instant now = Instant.now();
        Instant expiryTime = creationTime.plusMillis(duration);
        return now.isAfter(expiryTime);
    }
    
    /**
     * Gibt die entsprechende CSS-Farbklasse basierend auf dem Layer-Typ zurück.
     * Kann für die Visualisierung in Web-Anwendungen verwendet werden.
     * 
     * @return CSS-Klassenname
     */
    public String getCssColorClass() {
        switch (layerType) {
            case "RunningLayer":
                return "footstep-running";
            case "AdvancedWalkingLayer":
                return "footstep-advanced";
            case "BasicWalkingLayer":
                return "footstep-basic";
            case "IdleLayer":
                return "footstep-idle";
            default:
                return "footstep-default";
        }
    }
}
