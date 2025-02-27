package com.example.motion.visual.footstep;

import com.example.motion.sys.model.MotionState;
import com.example.motion.sys.model.Position;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FootstepVisualizer erstellt und verwaltet Fußspuren basierend auf Charakterbewegungen.
 * Fußspuren sind dynamisch und passen sich an das aktive Bewegungslayer an.
 */
public class FootstepVisualizer {
    private static final int MAX_FOOTSTEPS_PER_CHARACTER = 20;
    private static final float DEFAULT_FOOT_STRIDE = 1.5f; // Abstand zwischen den Fußspuren
    private static final float DEFAULT_FOOT_WIDTH = 0.25f;
    private static final float DEFAULT_FOOT_LENGTH = 0.45f;
    private static final long DEFAULT_FOOTSTEP_DURATION = 10000; // ms (Lebensdauer)
    
    private final ConcurrentHashMap<UUID, FootstepData> characterFootsteps;
    
    /**
     * Erstellt einen neuen FootstepVisualizer.
     */
    public FootstepVisualizer() {
        this.characterFootsteps = new ConcurrentHashMap<>();
    }
    
    /**
     * Aktualisiert die Fußspuren für einen Charakter basierend auf seinem Bewegungszustand.
     * 
     * @param state Aktueller Bewegungszustand des Charakters
     * @param layerType Typ des aktuellen Bewegungslayers
     * @return Liste der aktuellen Fußspuren zur Anzeige
     */
    public List<Footstep> updateFootsteps(MotionState state, String layerType) {
        UUID characterId = state.getCharacterId();
        Position currentPosition = state.getPosition();
        float speed = state.getSpeed();
        float yawAngle = state.getRotation().getYaw(); 
        
        // Keine Fußspuren erstellen, wenn der Charakter sich nicht bewegt
        if (speed <= 0.01f) {
            return getFootsteps(characterId);
        }
        
        // Daten für diesen Charakter abrufen oder erstellen
        FootstepData data = characterFootsteps.computeIfAbsent(characterId, id -> new FootstepData());
        
        // Aktualisiere die zurückgelegte Distanz
        data.updateDistanceTraveled(currentPosition);
        
        // Prüfe, ob eine neue Fußspur erstellt werden sollte
        float strideDistance = calculateStrideDistance(layerType, speed);
        
        if (data.getDistanceSinceLastFootstep() >= strideDistance) {
            // Erstelle eine neue Fußspur
            createNewFootstep(characterId, currentPosition, yawAngle, layerType, data);
            // Setze die Distanz zurück
            data.resetDistanceSinceLastFootstep();
        }
        
        // Führe Wartung durch (alte Fußspuren entfernen)
        cleanupOldFootsteps(characterId);
        
        // Gib die aktuelle Liste der Fußspuren zurück
        return getFootsteps(characterId);
    }
    
    /**
     * Berechnet den Abstand zwischen Fußspuren basierend auf Layer-Typ und Geschwindigkeit.
     */
    private float calculateStrideDistance(String layerType, float speed) {
        float baseStride = DEFAULT_FOOT_STRIDE;
        
        // Anpassung basierend auf Layer-Typ
        switch (layerType) {
            case "RunningLayer":
                // Größerer Abstand beim Laufen
                baseStride *= 1.5f;
                break;
            case "IdleLayer":
                // Keine Fußspuren im Idle-Zustand
                baseStride = Float.MAX_VALUE;
                break;
            case "AdvancedWalkingLayer":
                // Variiert je nach Gangart
                // Annahme: In diesem Fall weißt du nicht, welche Gangart aktiv ist
                baseStride *= 1.2f;
                break;
            default: // BasicWalkingLayer und andere
                // Standardabstand
                break;
        }
        
        // Skalierung mit der Geschwindigkeit
        return baseStride * (1.0f + 0.5f * speed);
    }
    
    /**
     * Erstellt eine neue Fußspur an der aktuellen Position des Charakters.
     */
    private void createNewFootstep(UUID characterId, Position position, float yawAngle, String layerType, FootstepData data) {
        boolean isLeftFoot = !data.isLastFootLeft();
        
        // Berechne Footstep-Parameter basierend auf Layer-Typ
        float width = DEFAULT_FOOT_WIDTH;
        float length = DEFAULT_FOOT_LENGTH;
        float opacity = 1.0f;
        long duration = DEFAULT_FOOTSTEP_DURATION;
        
        switch (layerType) {
            case "RunningLayer":
                // Tiefere, aber schmalere Abdrücke beim Laufen
                width *= 0.8f;
                length *= 1.2f;
                opacity *= 1.2f;
                duration *= 0.8f; // Kürzere Dauer
                break;
            case "AdvancedWalkingLayer":
                // Detailliertere Abdrücke
                width *= 1.1f;
                length *= 1.1f;
                opacity *= 1.1f;
                break;
            case "IdleLayer":
                // Sollte nicht vorkommen, da wir im Idle keine Fußspuren erstellen
                width *= 0.5f;
                length *= 0.5f;
                opacity *= 0.5f;
                break;
            default: // BasicWalkingLayer und andere
                // Standardwerte
                break;
        }
        
        // Berechne Position des Fußes (leicht versetzt zur Seite)
        float sideOffset = isLeftFoot ? -0.2f : 0.2f;
        
        // Berechne die tatsächliche Versetzung basierend auf der Rotation
        float offsetX = (float) (Math.cos(Math.toRadians(yawAngle + 90)) * sideOffset);
        float offsetZ = (float) (Math.sin(Math.toRadians(yawAngle + 90)) * sideOffset);
        
        Position footPosition = new Position(
            position.getX() + offsetX,
            position.getY(),
            position.getZ() + offsetZ
        );
        
        // Erstelle die Fußspur
        Footstep footstep = new Footstep(
            UUID.randomUUID(),
            characterId,
            footPosition,
            yawAngle,
            isLeftFoot,
            width,
            length,
            opacity,
            layerType,
            Instant.now(),
            duration
        );
        
        // Füge die Fußspur der Liste hinzu
        data.addFootstep(footstep);
        data.setLastFootLeft(isLeftFoot);
    }
    
    /**
     * Entfernt alte Fußspuren, die über ihre Lebensdauer hinaus sind.
     */
    private void cleanupOldFootsteps(UUID characterId) {
        FootstepData data = characterFootsteps.get(characterId);
        if (data == null) {
            return;
        }
        
        Instant now = Instant.now();
        Iterator<Footstep> iterator = data.getFootsteps().iterator();
        
        while (iterator.hasNext()) {
            Footstep footstep = iterator.next();
            Instant expiryTime = footstep.getCreationTime().plusMillis(footstep.getDuration());
            
            if (now.isAfter(expiryTime)) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Gibt alle aktuellen Fußspuren für einen Charakter zurück.
     */
    public List<Footstep> getFootsteps(UUID characterId) {
        FootstepData data = characterFootsteps.get(characterId);
        if (data == null) {
            return Collections.emptyList();
        }
        
        return new ArrayList<>(data.getFootsteps());
    }
    
    /**
     * Entfernt alle Fußspuren für einen Charakter.
     */
    public void clearFootsteps(UUID characterId) {
        characterFootsteps.remove(characterId);
    }
    
    /**
     * Entfernt alle Fußspuren für alle Charaktere.
     */
    public void clearAllFootsteps() {
        characterFootsteps.clear();
    }
    
    /**
     * Interne Klasse zur Speicherung der Fußspuren-Daten für einen Charakter.
     */
    @Data
    private static class FootstepData {
        private final List<Footstep> footsteps = new ArrayList<>();
        private Position lastPosition = null;
        private float distanceSinceLastFootstep = 0.0f;
        private boolean lastFootLeft = false;
        
        /**
         * Aktualisiert die zurückgelegte Distanz seit der letzten Position.
         */
        public void updateDistanceTraveled(Position currentPosition) {
            if (lastPosition != null) {
                float dx = currentPosition.getX() - lastPosition.getX();
                float dz = currentPosition.getZ() - lastPosition.getZ();
                float distance = (float) Math.sqrt(dx * dx + dz * dz);
                
                distanceSinceLastFootstep += distance;
            }
            
            lastPosition = currentPosition;
        }
        
        /**
         * Setzt die Distanz seit der letzten Fußspur zurück.
         */
        public void resetDistanceSinceLastFootstep() {
            distanceSinceLastFootstep = 0.0f;
        }
        
        /**
         * Fügt eine neue Fußspur hinzu und entfernt ältere, wenn das Limit erreicht ist.
         */
        public void addFootstep(Footstep footstep) {
            footsteps.add(footstep);
            
            // Halte die Anzahl der Fußspuren unter dem Limit
            while (footsteps.size() > MAX_FOOTSTEPS_PER_CHARACTER) {
                footsteps.remove(0);
            }
        }
    }
}
